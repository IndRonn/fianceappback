package com.finance.financeapp.dto.daily;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DailyStatusResponse {
    private LocalDate date;

    // El "Número Mágico": Cuánto puedo gastar HOY
    private BigDecimal availableForToday;

    // Contexto
    private BigDecimal totalMonthLimit; // Suma de todos los presupuestos variables
    private BigDecimal totalMonthSpent; // Lo que ya gasté en variables
    private int remainingDays;          // Días que faltan para acabar el mes

    // Semáforo Diario (Diferente al mensual)
    // ON_TRACK (Vas bien), OVERSPENT (Te pasaste hoy, mañana tendrás menos)
    private String status;
}