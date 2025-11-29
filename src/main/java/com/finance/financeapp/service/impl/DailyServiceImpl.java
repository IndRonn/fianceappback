package com.finance.financeapp.service.impl;

import com.finance.financeapp.domain.enums.ManagementType;
import com.finance.financeapp.domain.enums.TransactionType;
import com.finance.financeapp.dto.daily.DailyCloseRequest;
import com.finance.financeapp.dto.daily.DailyStatusResponse;
import com.finance.financeapp.dto.transaction.TransactionRequest;
import com.finance.financeapp.exception.custom.BusinessRuleException;
import com.finance.financeapp.exception.custom.ResourceNotFoundException;
import com.finance.financeapp.model.Budget;
import com.finance.financeapp.model.SavingsGoal;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.IBudgetRepository;
import com.finance.financeapp.repository.ISavingsGoalRepository;
import com.finance.financeapp.repository.ITransactionRepository;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.service.IDailyService;
import com.finance.financeapp.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyServiceImpl implements IDailyService {

    private final IBudgetRepository budgetRepository;
    private final ITransactionRepository transactionRepository;
    private final IUserRepository userRepository;
    private final ISavingsGoalRepository savingsGoalRepository;
    private final ITransactionService transactionService; // Reutilizamos lógica de transacciones

    // --- Helper Privado (Respuesta a tu duda 1) ---
    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado en contexto."));
    }

    @Override
    @Transactional(readOnly = true)
    public DailyStatusResponse getDailyStatus() {
        User user = getAuthenticatedUser(); // Usamos el helper aquí

        // 1. Fechas
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        // 2. Presupuestos Día a Día
        List<Budget> variableBudgets = budgetRepository.findByUserIdAndMonthAndYearAndType(
                user.getId(), today.getMonthValue(), today.getYear(), ManagementType.DIA_A_DIA
        );

        BigDecimal totalLimit = variableBudgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Gastos Reales
        BigDecimal totalSpent = transactionRepository.sumTotalVariableExpenses(
                user.getId(), startOfMonth, endOfMonth
        );

        // 4. Cálculos
        BigDecimal remainingBudget = totalLimit.subtract(totalSpent);
        int daysInMonth = currentMonth.lengthOfMonth();
        int daysPassed = today.getDayOfMonth() - 1;
        int remainingDays = daysInMonth - daysPassed;
        if (remainingDays <= 0) remainingDays = 1;

        BigDecimal availableToday = BigDecimal.ZERO;
        if (remainingBudget.compareTo(BigDecimal.ZERO) > 0) {
            availableToday = remainingBudget.divide(BigDecimal.valueOf(remainingDays), 2, RoundingMode.FLOOR);
        }

        String status = "ON_TRACK";
        if (remainingBudget.compareTo(BigDecimal.ZERO) < 0) status = "OVERSPENT";
        else if (availableToday.compareTo(BigDecimal.ZERO) == 0 && remainingDays > 0) status = "STOP";

        return DailyStatusResponse.builder()
                .date(today)
                .availableForToday(availableToday)
                .totalMonthLimit(totalLimit)
                .totalMonthSpent(totalSpent)
                .remainingDays(remainingDays)
                .status(status)
                .build();
    }

    @Override
    @Transactional
    public void closeDailyBox(DailyCloseRequest request) {
        User user = getAuthenticatedUser();

        if (request.getAction() == DailyCloseRequest.DailyCloseAction.ROLLOVER) {
            return; // No hacemos nada, el dinero se queda disponible para mañana
        }

        if (request.getAction() == DailyCloseRequest.DailyCloseAction.SAVE) {
            // Validaciones
            if (request.getTargetSavingsGoalId() == null) {
                throw new BusinessRuleException("Falta ID de Meta de Ahorro.");
            }
            if (request.getSourceAccountId() == null) {
                throw new BusinessRuleException("Falta ID de Cuenta Origen.");
            }
            if (request.getCategoryId() == null) {
                throw new BusinessRuleException("Falta ID de Categoría (ej. 'Ahorro') para registrar el movimiento.");
            }

            SavingsGoal goal = savingsGoalRepository.findById(request.getTargetSavingsGoalId())
                    .filter(g -> g.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Meta de ahorro no encontrada."));

            // 1. Crear Transacción de Gasto (Sacar el dinero de la cuenta)
            TransactionRequest trxReq = new TransactionRequest();
            trxReq.setAmount(request.getAmount());
            trxReq.setType(TransactionType.GASTO);
            trxReq.setAccountId(request.getSourceAccountId());
            trxReq.setTransactionDate(LocalDateTime.now());
            trxReq.setDescription("Cierre Diario -> Ahorro: " + goal.getName());

            // ¡AQUÍ ESTÁ LA RESPUESTA A TU DUDA 2!
            // Asignamos la categoría (ej: "Ahorro") que viene del frontend
            trxReq.setCategoryId(request.getCategoryId());

            // Usamos el servicio existente para que valide saldos y cree el registro
            transactionService.createTransaction(trxReq);

            // 2. Aumentar la Meta de Ahorro
            goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));
            savingsGoalRepository.save(goal);
        }
    }
}