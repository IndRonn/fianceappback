package com.finance.financeapp.dto.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class BudgetRequest {
    @NotNull
    private Long categoryId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @Min(1) @Max(12)
    private Integer month;

    @NotNull
    private Integer year;
}
