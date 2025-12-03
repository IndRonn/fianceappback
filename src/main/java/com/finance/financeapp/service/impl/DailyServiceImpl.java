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

        // Rangos de Tiempo
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        // Fin de ayer (Para calcular lo acumulado histórico)
        LocalDateTime endOfYesterday = today.minusDays(1).atTime(23, 59, 59);

        // Rango de HOY (Para restar directo)
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(23, 59, 59);

        // 2. Obtener Presupuesto Total Variable (La Bolsa)
        List<Budget> variableBudgets = budgetRepository.findByUserIdAndMonthAndYearAndType(
                user.getId(), today.getMonthValue(), today.getYear(), ManagementType.DIA_A_DIA
        );
        BigDecimal totalLimit = variableBudgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Obtener Gastos Separados (Pasado vs Presente)

        // A. Gastos Históricos (Desde el 1ro hasta Ayer)
        // Nota: Si hoy es día 1, esto debería ser 0 o manejar el rango correctamente.
        BigDecimal spentUntilYesterday = BigDecimal.ZERO;
        if (today.getDayOfMonth() > 1) {
            spentUntilYesterday = transactionRepository.sumTotalVariableExpenses(
                    user.getId(), startOfMonth, endOfYesterday
            );
        }
        if (spentUntilYesterday == null) spentUntilYesterday = BigDecimal.ZERO;

        // B. Gastos de HOY (Impacto Directo)
        BigDecimal spentToday = transactionRepository.sumTotalVariableExpenses(
                user.getId(), startOfToday, endOfToday
        );
        if (spentToday == null) spentToday = BigDecimal.ZERO;

        // C. Gasto Total (Solo para mostrar el acumulado en el dashboard)
        BigDecimal totalSpent = spentUntilYesterday.add(spentToday);

        // 4. El Nuevo Algoritmo "Modo Estricto"
        int daysInMonth = currentMonth.lengthOfMonth();
        int daysPassed = today.getDayOfMonth() - 1;
        int remainingDays = daysInMonth - daysPassed; // Incluye hoy

        if (remainingDays <= 0) remainingDays = 1;

        // Paso 1: ¿Con cuánto dinero amanecimos hoy?
        BigDecimal remainingStartOfToday = totalLimit.subtract(spentUntilYesterday);

        // Paso 2: Calcular la Cuota Base de Hoy (Antes de gastar nada hoy)
        // Esta es tu "Asignación Diaria Blindada"
        BigDecimal baseDailyBudget = BigDecimal.ZERO;
        if (remainingStartOfToday.compareTo(BigDecimal.ZERO) > 0) {
            baseDailyBudget = remainingStartOfToday.divide(
                    BigDecimal.valueOf(remainingDays), 2, RoundingMode.FLOOR
            );
        }

        // Paso 3: Resta Directa (Sin suavizado)
        // Disponible = Lo que me tocaba hoy - Lo que gasté hoy
        BigDecimal availableToday = baseDailyBudget.subtract(spentToday);

        // 5. Cálculos de Gamificación (Ayer y Mañana)

        // Gasto de Ayer (Para retrospectiva)
        // Requerimos consultar solo el rango de ayer específico
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();
        BigDecimal yesterdaySpent = transactionRepository.sumTotalVariableExpenses(
                user.getId(), startOfYesterday, endOfYesterday
        );
        if (yesterdaySpent == null) yesterdaySpent = BigDecimal.ZERO;

        // Cálculo de Ahorro de Ayer (Recalculando la base de ayer)
        BigDecimal yesterdaySaved = BigDecimal.ZERO;
        if (today.getDayOfMonth() > 1) {
            // Lógica simplificada: Si no guardamos histórico, re-calculamos.
            // Para no hacer 3 queries más, una aproximación aceptable es:
            // Ahorro = (Presupuesto / DíasTotales) - GastoAyer? NO, eso es impreciso.
            // Mantenemos la lógica anterior de yesterdaySaved o la simplificamos a 0 por ahora
            // para no sobrecargar la base de datos si no es crítico.
            // Dejaremos yesterdaySaved en 0 o requeriría la lógica compleja anterior.
            // Por eficiencia, usa la lógica que te di en el mensaje anterior para yesterdaySaved
            // o asume 0 si quieres ahorrar cómputo.
            // (Aquí pego la lógica completa para que no falte nada):

            LocalDateTime endOfBeforeYesterday = today.minusDays(2).atTime(23, 59, 59);
            BigDecimal spentBeforeYesterday = BigDecimal.ZERO;
            if (today.getDayOfMonth() > 2) {
                spentBeforeYesterday = transactionRepository.sumTotalVariableExpenses(
                        user.getId(), startOfMonth, endOfBeforeYesterday
                );
            }
            if (spentBeforeYesterday == null) spentBeforeYesterday = BigDecimal.ZERO;

            BigDecimal remainingStartYesterday = totalLimit.subtract(spentBeforeYesterday);
            int daysRemainingYesterday = daysInMonth - (today.getDayOfMonth() - 2);
            BigDecimal baseYesterday = remainingStartYesterday.divide(BigDecimal.valueOf(daysRemainingYesterday), 2, RoundingMode.FLOOR);
            yesterdaySaved = baseYesterday.subtract(yesterdaySpent);
        }

        // Proyección Mañana (Si hoy me porto bien y no gasto MÁS)
        // Mañana mi base será (remainingStartOfToday - spentToday) / (dias - 1)
        BigDecimal projectedTomorrow = BigDecimal.ZERO;
        if (remainingDays > 1) {
            BigDecimal remainingForTomorrow = remainingStartOfToday.subtract(spentToday);
            if (remainingForTomorrow.compareTo(BigDecimal.ZERO) > 0) {
                projectedTomorrow = remainingForTomorrow.divide(
                        BigDecimal.valueOf(remainingDays - 1), 2, RoundingMode.FLOOR
                );
            }
        }

        // 6. Semáforo
        String status = "ON_TRACK";
        if (remainingStartOfToday.compareTo(BigDecimal.ZERO) < 0) status = "OVERSPENT"; // Ya amanecí en rojo
        else if (availableToday.compareTo(BigDecimal.ZERO) < 0) status = "STOP"; // Me gasté la de hoy (y más)

        return DailyStatusResponse.builder()
                .date(today)
                .availableForToday(availableToday) // AHORA ES ESTRICTO
                .totalMonthLimit(totalLimit)
                .totalMonthSpent(totalSpent)
                .remainingDays(remainingDays)
                .status(status)
                .yesterdaySpent(yesterdaySpent)
                .projectedAvailableTomorrow(projectedTomorrow)
                .yesterdaySaved(yesterdaySaved)
                .dailyLimit(baseDailyBudget) // Tu "11.83" original
                .spentToday(spentToday)
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