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
    private final ITransactionService transactionService;

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado en contexto."));
    }

    @Override
    @Transactional(readOnly = true)
    public DailyStatusResponse getDailyStatus() {
        User user = getAuthenticatedUser();

        // 1. Fechas Base
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        // Rango Mes Actual
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        // Rango Ayer (Para retrospectiva)
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = today.minusDays(1).atTime(23, 59, 59);

        // 2. Obtener Presupuestos Día a Día (Planificado Total)
        List<Budget> variableBudgets = budgetRepository.findByUserIdAndMonthAndYearAndType(
                user.getId(), today.getMonthValue(), today.getYear(), ManagementType.DIA_A_DIA
        );

        BigDecimal totalLimit = variableBudgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Gastos Reales (Acumulado Mes)
        BigDecimal totalSpent = transactionRepository.sumTotalVariableExpenses(
                user.getId(), startOfMonth, endOfMonth
        );

        // --- NUEVO CÁLCULO: Gasto de Ayer ---
        BigDecimal yesterdaySpent = transactionRepository.sumTotalVariableExpenses(
                user.getId(), startOfYesterday, endOfYesterday
        );

        // 4. Cálculos de Disponibilidad
        BigDecimal remainingBudget = totalLimit.subtract(totalSpent);
        int daysInMonth = currentMonth.lengthOfMonth();
        int daysPassed = today.getDayOfMonth() - 1;
        int remainingDays = daysInMonth - daysPassed;

        if (remainingDays <= 0) remainingDays = 1;

        // Disponible HOY
        BigDecimal availableToday = BigDecimal.ZERO;
        if (remainingBudget.compareTo(BigDecimal.ZERO) > 0) {
            availableToday = remainingBudget.divide(BigDecimal.valueOf(remainingDays), 2, RoundingMode.FLOOR);
        }

        // --- NUEVO CÁLCULO: Proyección Mañana (Motivación) ---
        // Si no gastas nada hoy, esto tendrás mañana.
        BigDecimal projectedTomorrow = BigDecimal.ZERO;

        // Lógica: Si hoy no gasto, mañana tendré el mismo remainingBudget, pero dividido entre (días - 1).
        if (remainingDays > 1 && remainingBudget.compareTo(BigDecimal.ZERO) > 0) {
            projectedTomorrow = remainingBudget.divide(
                    BigDecimal.valueOf(remainingDays - 1),
                    2,
                    RoundingMode.FLOOR
            );
        } else if (remainingDays == 1) {
            // Si es el último día, lo que sobra es ahorro puro (o disponible del 1ro del sig mes, según se vea)
            projectedTomorrow = remainingBudget;
        }

        // 5. Semáforo
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
                // Nuevos campos
                .yesterdaySpent(yesterdaySpent)
                .projectedAvailableTomorrow(projectedTomorrow)
                .build();
    }

    @Override
    @Transactional
    public void closeDailyBox(DailyCloseRequest request) {
        User user = getAuthenticatedUser();

        if (request.getAction() == DailyCloseRequest.DailyCloseAction.ROLLOVER) {
            return;
        }

        if (request.getAction() == DailyCloseRequest.DailyCloseAction.SAVE) {
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

            TransactionRequest trxReq = new TransactionRequest();
            trxReq.setAmount(request.getAmount());
            trxReq.setType(TransactionType.GASTO);
            trxReq.setAccountId(request.getSourceAccountId());
            trxReq.setTransactionDate(LocalDateTime.now());
            trxReq.setDescription("Cierre Diario -> Ahorro: " + goal.getName());
            trxReq.setCategoryId(request.getCategoryId());

            transactionService.createTransaction(trxReq);

            goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));
            savingsGoalRepository.save(goal);
        }
    }
}