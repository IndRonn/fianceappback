package com.finance.financeapp.dto.daily;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailyCloseRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount; // El monto sobrante que queremos procesar

    // Acción a tomar: "SAVE" (Ahorrar) o "ROLLOVER" (Dejarlo para mañana - no hace nada en BD)
    @NotNull
    private DailyCloseAction action;

    // Opcional: Si la acción es SAVE, ¿a qué meta va?
    private Long targetSavingsGoalId;

    // Opcional: ¿De qué cuenta física sacamos el dinero? (Para integridad Hard Mode)
    private Long sourceAccountId;

    public enum DailyCloseAction {
        SAVE,
        ROLLOVER
    }

    private Long categoryId;
}