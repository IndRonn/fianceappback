package com.finance.financeapp.service.impl;

import com.finance.financeapp.domain.enums.ManagementType;
import com.finance.financeapp.dto.daily.DailyStatusResponse;
import com.finance.financeapp.model.Budget;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.IBudgetRepository;
import com.finance.financeapp.repository.ITransactionRepository;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.service.IDailyService; // ¡Crea la interfaz primero!
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

    @Override
    @Transactional(readOnly = true)
    public DailyStatusResponse getDailyStatus() {
        // 1. Usuario Autenticado
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Fechas (Hoy y Fin de Mes)
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        // 3. Obtener Presupuestos "Día a Día" (Planificado Total)
        List<Budget> variableBudgets = budgetRepository.findByUserIdAndMonthAndYearAndType(
                user.getId(),
                today.getMonthValue(),
                today.getYear(),
                ManagementType.DIA_A_DIA
        );

        BigDecimal totalLimit = variableBudgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Obtener Gastos Reales en esas categorías (Lo que ya se fue)
        BigDecimal totalSpent = transactionRepository.sumTotalVariableExpenses(
                user.getId(),
                startOfMonth,
                endOfMonth
        );

        // 5. Cálculos Mágicos
        BigDecimal remainingBudget = totalLimit.subtract(totalSpent);
        int daysInMonth = currentMonth.lengthOfMonth();
        int daysPassed = today.getDayOfMonth() - 1; // Días completos pasados
        int remainingDays = daysInMonth - daysPassed; // Incluye hoy

        // Evitar división por cero (si alguien pide esto el último segundo del mes)
        if (remainingDays <= 0) remainingDays = 1;

        // Fórmula: ¿Cuánto puedo gastar HOY?
        // Si ya gasté de más (negativo), disponible es 0.
        BigDecimal availableToday = BigDecimal.ZERO;
        if (remainingBudget.compareTo(BigDecimal.ZERO) > 0) {
            availableToday = remainingBudget.divide(
                    BigDecimal.valueOf(remainingDays),
                    2,
                    RoundingMode.FLOOR // Conservador: mejor que sobre centavos a que falten
            );
        }

        // 6. Semáforo Diario
        String status = "ON_TRACK";
        if (remainingBudget.compareTo(BigDecimal.ZERO) < 0) {
            status = "OVERSPENT"; // Ya estás en rojo global
        } else if (availableToday.compareTo(BigDecimal.ZERO) == 0 && remainingDays > 0) {
            status = "STOP"; // Tienes saldo, pero no para hoy (o muy poco)
        }

        return DailyStatusResponse.builder()
                .date(today)
                .availableForToday(availableToday)
                .totalMonthLimit(totalLimit)
                .totalMonthSpent(totalSpent)
                .remainingDays(remainingDays)
                .status(status)
                .build();
    }
}