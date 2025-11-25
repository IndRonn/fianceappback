package com.finance.financeapp.dto.transaction;

import com.finance.financeapp.domain.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;

    // Devolvemos nombres e IDs para facilitar la UI sin traer objetos anidados complejos
    private Long accountId;
    private String accountName;

    private Long categoryId;
    private String categoryName;

    private Long destinationAccountId;
    private String destinationAccountName;
}