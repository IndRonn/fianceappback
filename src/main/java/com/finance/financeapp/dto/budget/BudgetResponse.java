package com.finance.financeapp.dto.budget;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetResponse {
    private Long id;
    private String categoryName;

    // Mapeado desde entity.getAmount()
    private BigDecimal limitAmount;

    // Calculado (Suma de transacciones)
    private BigDecimal spentAmount;

    // Calculado (limit - spent)
    private BigDecimal remainingAmount;

    // Calculado (spent / limit * 100)
    private double percentage;

    // Calculado (OK, WARNING, DANGER)
    private String status; // <--- ¡Aquí está!
}