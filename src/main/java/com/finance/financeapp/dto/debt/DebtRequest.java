package com.finance.financeapp.dto.debt;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DebtRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String name;

    @Size(max = 100)
    private String creditor;

    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal totalAmount;

    // Opcional: saldo inicial si ya pagaste algo antes
    private BigDecimal currentBalance;
}