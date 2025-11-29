package com.finance.financeapp.dto.debt;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DebtResponse {
    private Long id;
    private String name;
    private String creditor;
    private BigDecimal totalAmount;
    private BigDecimal currentBalance;
    private double progressPercentage;
}