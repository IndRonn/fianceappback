package com.finance.financeapp.dto.goal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SavingsGoalRequest {

    @NotBlank(message = "El nombre de la meta es obligatorio")
    @Size(max = 150)
    private String name;

    // Opcional: Puede ser una meta sin límite ("Ahorro general")
    @DecimalMin(value = "1.00", message = "La meta debe ser mayor a 1")
    private BigDecimal targetAmount;

    // Opcional: Saldo inicial (por defecto 0 si es null)
    private BigDecimal initialAmount;
}