package com.finance.financeapp.service.impl;

import com.finance.financeapp.domain.enums.AccountType;
import com.finance.financeapp.dto.account.AccountRequest;
import com.finance.financeapp.dto.account.AccountResponse;
import com.finance.financeapp.exception.custom.BusinessRuleException;
import com.finance.financeapp.mapper.AccountMapper;
import com.finance.financeapp.model.Account;
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
    private final AccountMapper accountMapper; // Inyección del Mapper
    private final ITransactionRepository transactionRepository;

    // --- Helper Methods ---

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en contexto."));
    }

    private Account findAccountAndVerifyOwnership(Long accountId, Long userId) {
        return accountRepository.findById(accountId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada o acceso denegado."));
    }

    // --- CRUD Implementations ---

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        User user = getAuthenticatedUser();

        // 1. VALIDACIÓN HARD MODE: Integridad de Tarjetas de Crédito
        // Si es tarjeta de crédito, EXIGIMOS límite y fechas de corte.
        if (request.getType() == AccountType.CREDITO) {
            if (request.getCreditLimit() == null) {
                throw new BusinessRuleException("El límite de crédito es obligatorio para tarjetas de crédito.");
            }
            if (request.getClosingDate() == null || request.getPaymentDate() == null) {
                throw new BusinessRuleException("Las fechas de corte y pago son obligatorias para tarjetas de crédito.");
            }
        }

        // 2. Convertir y Asignar
        Account account = accountMapper.toEntity(request);
        account.setUser(user);

        // 3. Guardar y Retornar
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {
        User user = getAuthenticatedUser();
        List<Account> accounts = accountRepository.findByUserId(user.getId());

        // Mapeamos y calculamos el statementBalance para cada cuenta
        return accounts.stream()
                .map(account -> {
                    AccountResponse response = accountMapper.toResponse(account);

                    // Lógica solo para Tarjetas de Crédito
                    if (account.getType() == AccountType.CREDITO && account.getClosingDate() != null) {
                        BigDecimal statementBalance = calculateBillingCycleBalance(account);
                        response.setStatementBalance(statementBalance);
                    } else {
                        // Para débito/efectivo, el statement balance no aplica (o es igual al saldo)
                        response.setStatementBalance(BigDecimal.ZERO);
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateBillingCycleBalance(Account account) {
        LocalDate today = LocalDate.now();
        int closingDay = account.getClosingDate();

        // Determinar fechas del ciclo
        LocalDateTime startOfCycle;
        LocalDateTime endOfCycle;

        // Lógica de fechas (Caso A vs Caso B)
        if (today.getDayOfMonth() <= closingDay) {
            // CASO A: Ciclo Abierto (Estamos antes del cierre de este mes)
            // El ciclo empezó el mes pasado (día cierre + 1)
            // Termina HOY (para ver el acumulado al momento)
            LocalDate lastMonthDate = today.minusMonths(1);

            // Manejo de bordes (ej: cierre día 31 en mes de 30 días)
            int safeClosingDay = Math.min(closingDay, lastMonthDate.lengthOfMonth());

            startOfCycle = lastMonthDate.withDayOfMonth(safeClosingDay).plusDays(1).atStartOfDay();
            endOfCycle = LocalDateTime.now(); // Hasta el momento actual
        } else {
            // CASO B: Ciclo Cerrado (Estamos después del cierre)
            // El ciclo empezó este mes (día cierre del mes pasado + 1... espera, no)
            // Si hoy es 25 y cierra el 20:
            // El ciclo que YA CERRÓ fue del 21 del mes pasado al 20 de este mes.
            // Pero el usuario quiere saber "¿Qué estoy acumulando para el PRÓXIMO?" o "¿Qué debo pagar YA?"

            // Según tu requerimiento: "Caso B: Ciclo Cerrado... suma estática... hasta ClosingDate este mes".
            // Esto significa que mostramos la DEUDA YA CERRADA que se debe pagar pronto.

            LocalDate lastMonthDate = today.minusMonths(1);
            int safeLastClosing = Math.min(closingDay, lastMonthDate.lengthOfMonth());

            startOfCycle = lastMonthDate.withDayOfMonth(safeLastClosing).plusDays(1).atStartOfDay();
            endOfCycle = today.withDayOfMonth(closingDay).atTime(23, 59, 59);
        }

        // Ejecutar consulta
        return transactionRepository.getCycleExpenses(account.getId(), startOfCycle, endOfCycle);
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(Long accountId, AccountRequest request) {
        User user = getAuthenticatedUser();

        // 1. Verificar existencia y propiedad
        Account account = findAccountAndVerifyOwnership(accountId, user.getId());

        // 2. VALIDACIÓN HARD MODE EN UPDATE
        // Si la cuenta ES o SE VUELVE de Crédito, validamos la integridad
        if (request.getType() == AccountType.CREDITO) {
            // Caso A: Era Débito y ahora es Crédito -> Debe traer límite
            if (account.getType() != AccountType.CREDITO && request.getCreditLimit() == null) {
                throw new BusinessRuleException("Al cambiar a tipo CRÉDITO, debes especificar un límite.");
            }
            // Caso B: Ya era Crédito -> Si manda fechas nulas, error (si intenta borrarlas)
            // (Esta validación depende de si tu DTO permite nulos parciales, por ahora aseguramos integridad básica)
        }

        // 3. Actualizar la entidad
        accountMapper.updateEntityFromRequest(request, account);

        // 4. Doble Check post-mapeo (Cinturón y Tirantes)
        if (account.getType() == AccountType.CREDITO && account.getCreditLimit() == null) {
            throw new BusinessRuleException("La tarjeta de crédito no puede quedar sin límite.");
        }

        // 5. Guardar
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