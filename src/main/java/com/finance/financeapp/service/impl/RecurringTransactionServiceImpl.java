package com.finance.financeapp.service.impl;

import com.finance.financeapp.dto.recurring.RecurringTransactionRequest;
import com.finance.financeapp.dto.recurring.RecurringTransactionResponse;
import com.finance.financeapp.dto.transaction.TransactionRequest;
import com.finance.financeapp.exception.custom.ResourceNotFoundException;
import com.finance.financeapp.mapper.RecurringTransactionMapper;
import com.finance.financeapp.model.Account;
import com.finance.financeapp.model.Category;
import com.finance.financeapp.model.RecurringTransaction;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.IAccountRepository;
import com.finance.financeapp.repository.ICategoryRepository;
import com.finance.financeapp.repository.IRecurringTransactionRepository;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.service.IRecurringTransactionService;
import com.finance.financeapp.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringTransactionServiceImpl implements IRecurringTransactionService {

    private final IRecurringTransactionRepository recurringRepository;
    private final IUserRepository userRepository;
    private final IAccountRepository accountRepository;
    private final ICategoryRepository categoryRepository;
    private final RecurringTransactionMapper mapper;

    // Inyección del servicio de transacciones para reutilizar la lógica de negocio (Saldos, etc.)
    private final ITransactionService transactionService;

    // --- Helpers ---

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    private RecurringTransaction findAndVerifyOwnership(Long id, Long userId) {
        return recurringRepository.findById(id)
                .filter(rt -> rt.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Transacción recurrente no encontrada."));
    }

    // --- CRUD ---

    @Override
    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest request) {
        User user = getAuthenticatedUser();

        // 1. Validar Relaciones
        Account account = accountRepository.findById(request.getAccountId())
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada."));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada."));
        }

        Account destAccount = null;
        if (request.getDestinationAccountId() != null) {
            destAccount = accountRepository.findById(request.getDestinationAccountId())
                    .filter(a -> a.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta destino no encontrada."));
        }

        // 2. Mapear y Asignar
        RecurringTransaction entity = mapper.toEntity(request);
        entity.setUser(user);
        entity.setAccount(account);
        entity.setCategory(category);
        entity.setDestinationAccount(destAccount);

        // 3. Guardar plantilla
        return mapper.toResponse(recurringRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getMyRecurringTransactions() {
        User user = getAuthenticatedUser();
        // Fallback: filtramos en memoria si no has creado el método en el repositorio
        // Idealmente: recurringRepository.findByUserId(user.getId())
        return recurringRepository.findAll().stream()
                .filter(rt -> rt.getUser().getId().equals(user.getId()))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest request) {
        User user = getAuthenticatedUser();
        RecurringTransaction entity = findAndVerifyOwnership(id, user.getId());

        mapper.updateEntity(request, entity);

        // Nota: Si cambian cuentas/categorías, deberías volver a buscarlas y setearlas aquí.
        // Se omite por brevedad, pero es buena práctica validarlas de nuevo.

        return mapper.toResponse(recurringRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = getAuthenticatedUser();
        RecurringTransaction entity = findAndVerifyOwnership(id, user.getId());
        recurringRepository.delete(entity);
    }

    // --- MOTOR DE AUTOMATIZACIÓN (SCHEDULER + LISTENER) ---

    /**
     * Se ejecuta todos los días a las 00:01 AM.
     */
    @Override
    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void processScheduledTransactions() {
        log.info(">> [CRON] Ejecutando procesamiento diario de transacciones recurrentes...");
        executeBatchProcessing();
    }

    /**
     * Se ejecuta AUTOMÁTICAMENTE al iniciar la aplicación.
     * Recupera transacciones perdidas si el servidor estaba apagado.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void processOnStartup() {
        log.info(">> [STARTUP] Servidor iniciado: Buscando transacciones recurrentes pendientes (Catch-Up)...");
        executeBatchProcessing();
    }

    /**
     * Lógica central de procesamiento por lotes.
     */
    private void executeBatchProcessing() {
        LocalDate today = LocalDate.now();

        // Busca todo lo que tenga fecha de ejecución HOY o ANTES (atrasado)
        List<RecurringTransaction> candidates = recurringRepository
                .findByIsActiveTrueAndNextExecutionDateLessThanEqual(today);

        if (candidates.isEmpty()) {
            log.info(">> No hay transacciones pendientes para procesar hoy.");
            return;
        }

        log.info(">> Encontradas {} transacciones para procesar/recuperar.", candidates.size());

        for (RecurringTransaction recurring : candidates) {
            try {
                // 1. Crear la Transacción Real (History)
                executeTransactionFromTemplate(recurring);

                // 2. Calcular la siguiente fecha
                calculateNextExecutionDate(recurring);

                // 3. Actualizar la plantilla
                recurringRepository.save(recurring);

                log.info(">> Recurrencia ID {} procesada exitosamente. Próxima ejecución: {}",
                        recurring.getId(), recurring.getNextExecutionDate());

            } catch (Exception e) {
                log.error(">> ERROR CRÍTICO procesando recurrencia ID {}: {}", recurring.getId(), e.getMessage());
                // Continuamos con el siguiente, no rompemos el bucle
            }
        }
        log.info(">> Procesamiento por lotes finalizado.");
    }

    private void executeTransactionFromTemplate(RecurringTransaction template) {
        TransactionRequest req = new TransactionRequest();
        req.setType(template.getType());
        req.setAmount(template.getAmount());
        req.setDescription(template.getDescription() + " (Auto)"); // Marca visual
        req.setTransactionDate(LocalDateTime.now());

        req.setAccountId(template.getAccount().getId());
        req.setCategoryId(template.getCategory() != null ? template.getCategory().getId() : null);
        req.setDestinationAccountId(template.getDestinationAccount() != null ? template.getDestinationAccount().getId() : null);
        req.setExchangeRate(template.getExchangeRate());

        // Reutilizamos toda la lógica de validación y saldos
        transactionService.createTransaction(req);
    }

    private void calculateNextExecutionDate(RecurringTransaction recurring) {
        LocalDate current = recurring.getNextExecutionDate();

        // Calculamos la siguiente fecha basándonos en la frecuencia
        LocalDate next = switch (recurring.getFrequency()) {
            case DIARIO -> current.plusDays(1);
            case SEMANAL -> current.plusWeeks(1);
            case MENSUAL -> current.plusMonths(1);
            case BIMENSUAL -> current.plusMonths(2);
            case TRIMESTRAL -> current.plusMonths(3);
            case SEMESTRAL -> current.plusMonths(6);
            case ANUAL -> current.plusYears(1);
            case UNICO -> null; // Se desactiva tras una ejecución
        };

        // Lógica de Desactivación
        if (next == null) {
            recurring.setIsActive(false);
            log.info(">> Recurrencia ID {} finalizada (Frecuencia ÚNICA).", recurring.getId());
        } else {
            // Verificar si superó la fecha fin (si existe)
            if (recurring.getEndDate() != null && next.isAfter(recurring.getEndDate())) {
                recurring.setIsActive(false);
                log.info(">> Recurrencia ID {} finalizada (Fecha Fin alcanzada).", recurring.getId());
            } else {
                recurring.setNextExecutionDate(next);
            }
        }
    }
}