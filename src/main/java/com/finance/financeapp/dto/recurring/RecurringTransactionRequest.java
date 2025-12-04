package com.finance.financeapp.dto.recurring;

import com.finance.financeapp.domain.enums.Frequency;
import com.finance.financeapp.domain.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecurringTransactionRequest {

    @NotNull(message = "El tipo de transacción es obligatorio")
    private TransactionType type;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser positivo")
    private BigDecimal amount;

    @Size(max = 4000)
    private String description;

    // --- Configuración de Recurrencia ---

    @NotNull(message = "La frecuencia es obligatoria")
    private Frequency frequency;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    private LocalDate endDate; // Opcional (puede ser indefinido)

    // --- Relaciones ---

    @NotNull(message = "La cuenta origen es obligatoria")
    private Long accountId;

    private Long categoryId;

    private Long destinationAccountId; // Solo para transferencias
}