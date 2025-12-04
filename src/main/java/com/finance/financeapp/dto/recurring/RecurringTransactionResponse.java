package com.finance.financeapp.dto.recurring;

import com.finance.financeapp.domain.enums.Frequency;
import com.finance.financeapp.domain.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class RecurringTransactionResponse {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;

    // Info Recurrencia
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate nextExecutionDate;
    private LocalDate endDate;
    private Boolean isActive;

    // Info Relacionada (Solo Nombres e IDs)
    private Long accountId;
    private String accountName;
    private Long categoryId;
    private String categoryName;
    private Long destinationAccountId;
    private String destinationAccountName;
}