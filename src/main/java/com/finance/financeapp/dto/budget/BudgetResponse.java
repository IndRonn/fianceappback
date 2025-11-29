package com.finance.financeapp.dto.budget;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetResponse {
    private Long id;
    private String categoryName;
    private BigDecimal limitAmount; // Lo planificado
    private BigDecimal spentAmount; // La realidad (Calculado)
    private BigDecimal remainingAmount; // Lo que sobra (Calculado)
    private double percentage; // Para la barra de progreso
}