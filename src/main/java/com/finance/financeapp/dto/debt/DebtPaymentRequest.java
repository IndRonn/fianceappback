package com.finance.financeapp.dto.debt;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DebtPaymentRequest {
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount; // Cuánto pagas

    @NotNull
    private Long sourceAccountId; // De dónde sale

    // Categoría para registrar el gasto en historial (ej: "Deudas")
    @NotNull(message = "Categoría obligatoria")
    private Long categoryId;
}