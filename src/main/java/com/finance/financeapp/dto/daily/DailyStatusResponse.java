package com.finance.financeapp.dto.daily;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DailyStatusResponse {
    private LocalDate date;

    // Métricas de Hoy
    private BigDecimal availableForToday;
    private BigDecimal totalMonthLimit;
    private BigDecimal totalMonthSpent;
    private int remainingDays;
    private String status;

    // Gamificación
    private BigDecimal yesterdaySpent;
    private BigDecimal projectedAvailableTomorrow;

    // [NUEVO] El dato que pide el Frontend
    private BigDecimal yesterdaySaved;

    private BigDecimal dailyLimit;

    // [NUEVO] Lo que has gastado HOY
    // Ejemplo: 2.00
    private BigDecimal spentToday;// (DisponibleAyer - GastoAyer)
}