package com.finance.financeapp.service.impl;

import com.finance.financeapp.domain.enums.AccountType;
import com.finance.financeapp.domain.enums.TransactionType;
import com.finance.financeapp.dto.account.AccountRequest;
import com.finance.financeapp.dto.account.AccountResponse;
import com.finance.financeapp.exception.custom.BusinessRuleException;
import com.finance.financeapp.exception.custom.ResourceNotFoundException;
import com.finance.financeapp.mapper.AccountMapper;
import com.finance.financeapp.model.Account;
import com.finance.financeapp.model.Transaction;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.IAccountRepository;
import com.finance.financeapp.repository.ITransactionRepository;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.service.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements IAccountService {

    private final IAccountRepository accountRepository;
    private final IUserRepository userRepository;
    private final AccountMapper accountMapper;
    private final ITransactionRepository transactionRepository; // Inyección necesaria para la inicialización

    // --- Helper Methods ---

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado en contexto."));
    }

    private Account findAccountAndVerifyOwnership(Long accountId, Long userId) {
        return accountRepository.findById(accountId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada o acceso denegado."));
    }

    // --- Lógica de Inicialización de Tarjetas (Métodos Privados) ---

    private void initializeCreditCardHistory(Account account, AccountRequest request, User user) {
        // A. Carga del Ciclo ANTERIOR (Fecha: Cierre mes pasado - 1 día)
        if (request.getPreviousBalance() != null && request.getPreviousBalance().compareTo(BigDecimal.ZERO) > 0) {
            // Calcular fecha segura en el pasado
            int closingDay = account.getClosingDate() != null ? account.getClosingDate() : 15;
            LocalDate pastDate = LocalDate.now().minusMonths(1).withDayOfMonth(closingDay).minusDays(1);

            createInitializationTransaction(account, request.getPreviousBalance(), pastDate.atStartOfDay(), "Saldo Inicial - Ciclo Anterior");
        }

        // B. Carga del Ciclo ACTUAL (Fecha: Hoy)
        if (request.getCurrentBalance() != null && request.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
            createInitializationTransaction(account, request.getCurrentBalance(), LocalDateTime.now(), "Saldo Inicial - Ciclo Actual");
        }
    }

    private void createInitializationTransaction(Account account, BigDecimal amount, LocalDateTime date, String desc) {
        Transaction trx = Transaction.builder()
                .account(account)
                .amount(amount)
                .type(TransactionType.GASTO)
                .description(desc)
                .transactionDate(date)
                .exchangeRate(BigDecimal.ONE)
                .user(account.getUser())
                .build();

        // Guardamos la transacción
        transactionRepository.save(trx);

        // Actualizamos el saldo de la cuenta (Deuda sube)
        account.setInitialBalance(account.getInitialBalance().add(amount));
        accountRepository.save(account);
    }

    /**
     * Calcula el consumo del ciclo de facturación actual o cerrado.
     */
    private BigDecimal calculateBillingCycleBalance(Account account) {
        LocalDate today = LocalDate.now();
        int closingDay = account.getClosingDate() != null ? account.getClosingDate() : 15;

        LocalDateTime cutoffDate;

        if (today.getDayOfMonth() <= closingDay) {
            // CASO A: Ciclo Abierto (Ej: Hoy 2 Dic, Cierre 15)
            // El usuario aún está comprando para el ciclo de Diciembre.
            // El "Recibo" que debe pagar ahora es el que cerró en NOVIEMBRE.

            LocalDate lastMonth = today.minusMonths(1);
            int safeClosingDay = Math.min(closingDay, lastMonth.lengthOfMonth());

            // Corte: 15 de Noviembre 23:59:59
            cutoffDate = lastMonth.withDayOfMonth(safeClosingDay).atTime(23, 59, 59);
        } else {
            // CASO B: Ciclo Cerrado (Ej: Hoy 16 Dic, Cierre 15)
            // El ciclo de Diciembre ya cerró ayer.
            // El "Recibo" a pagar es el de DICIEMBRE.

            // Corte: 15 de Diciembre 23:59:59
            cutoffDate = today.withDayOfMonth(closingDay).atTime(23, 59, 59);
        }

        // Ejecutamos la suma "Hasta el Corte"
        // Esto incluirá la "Deuda Histórica" (transacciones antiguas) y los gastos del ciclo cerrado.
        // EXCLUIRÁ todo lo que hayas comprado después de esa fecha (ej: hoy).
        return transactionRepository.getExpensesUpTo(account.getId(), cutoffDate);
    }

    // --- CRUD Implementations ---

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        User user = getAuthenticatedUser();

        // 1. VALIDACIÓN HARD MODE
        if (request.getType() == AccountType.CREDITO) {
            if (request.getCreditLimit() == null) {
                throw new BusinessRuleException("El límite de crédito es obligatorio para tarjetas de crédito.");
            }
            if (request.getClosingDate() == null || request.getPaymentDate() == null) {
                throw new BusinessRuleException("Las fechas de corte y pago son obligatorias para tarjetas de crédito.");
            }
        }

        // 2. Mapeo (AQUÍ ESTÁ LA LÍNEA QUE FALTABA)
        Account account = accountMapper.toEntity(request);
        account.setUser(user);

        // 3. Lógica de Inicialización de Saldo para TC
        // Al crear, forzamos saldo 0 si es TC, porque lo llenaremos con transacciones a continuación
        if (request.getType() == AccountType.CREDITO) {
            account.setInitialBalance(BigDecimal.ZERO);
        }

        // 4. Guardar Entidad Base
        Account savedAccount = accountRepository.save(account);

        // 5. Generar Historial (Si aplica)
        if (request.getType() == AccountType.CREDITO) {
            initializeCreditCardHistory(savedAccount, request, user);
        }

        // 6. Retornar
        return accountMapper.toResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {
        User user = getAuthenticatedUser();
        List<Account> accounts = accountRepository.findByUserId(user.getId());

        return accounts.stream()
                .map(account -> {
                    AccountResponse response = accountMapper.toResponse(account);

                    if (account.getType() == AccountType.CREDITO && account.getClosingDate() != null) {
                        // 1. Calcular Deuda Facturada (Corte)
                        BigDecimal statementBalance = calculateBillingCycleBalance(account);
                        response.setStatementBalance(statementBalance);

                        // 2. Calcular Deuda Ciclo Actual (Total - Facturada)
                        // Si initialBalance es la deuda total, y statement es la vieja...
                        // La diferencia es lo nuevo.
                        BigDecimal totalDebt = account.getInitialBalance();
                        BigDecimal currentCycle = totalDebt.subtract(statementBalance);

                        // Validación visual (por si acaso hay inconsistencias de datos negativos)
                        if (currentCycle.compareTo(BigDecimal.ZERO) < 0) currentCycle = BigDecimal.ZERO;

                        response.setCurrentCycleBalance(currentCycle);

                    } else {
                        response.setStatementBalance(BigDecimal.ZERO);
                        response.setCurrentCycleBalance(BigDecimal.ZERO);
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(Long accountId, AccountRequest request) {
        User user = getAuthenticatedUser();
        Account account = findAccountAndVerifyOwnership(accountId, user.getId());

        // Validaciones de Integridad en Update
        if (request.getType() == AccountType.CREDITO) {
            if (account.getType() != AccountType.CREDITO && request.getCreditLimit() == null) {
                throw new BusinessRuleException("Al cambiar a tipo CRÉDITO, debes especificar un límite.");
            }
        }

        accountMapper.updateEntityFromRequest(request, account);

        if (account.getType() == AccountType.CREDITO && account.getCreditLimit() == null) {
            throw new BusinessRuleException("La tarjeta de crédito no puede quedar sin límite.");
        }

        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        User user = getAuthenticatedUser();
        Account account = findAccountAndVerifyOwnership(accountId, user.getId());
        accountRepository.delete(account);
    }
}