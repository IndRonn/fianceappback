package com.finance.financeapp.mapper;

import com.finance.financeapp.dto.transaction.TransactionRequest;
import com.finance.financeapp.dto.transaction.TransactionResponse;
import com.finance.financeapp.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequest request) {
        if (request == null) return null;

        // Nota: Las relaciones (User, Account, Category) se setean en el Service
        return Transaction.builder()
                .type(request.getType())
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) return null;

        return TransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .accountId(transaction.getAccount().getId())
                .accountName(transaction.getAccount().getName())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .destinationAccountId(transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getId() : null)
                .destinationAccountName(transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getName() : null)
                .build();
    }
}