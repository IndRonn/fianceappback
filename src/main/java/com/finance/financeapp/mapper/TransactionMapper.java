package com.finance.financeapp.mapper;

import com.finance.financeapp.dto.tag.TagResponse; // Asegúrate de importar esto
import com.finance.financeapp.dto.transaction.TransactionRequest;
import com.finance.financeapp.dto.transaction.TransactionResponse;
import com.finance.financeapp.model.Tag;
import com.finance.financeapp.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequest request) {
        if (request == null) return null;

        return Transaction.builder()
                .type(request.getType())
                .amount(request.getAmount())
                .exchangeRate(request.getExchangeRate() != null ? request.getExchangeRate() : java.math.BigDecimal.ONE) // Default 1.0
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                // NOTA: No mapeamos tags ni cuentas aquí, el Service lo hace.
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) return null;

        return TransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .exchangeRate(transaction.getExchangeRate())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate()) // O getTransactionDate() si usas Lombok Getter estándar
                .accountId(transaction.getAccount().getId())
                .accountName(transaction.getAccount().getName())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .destinationAccountId(transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getId() : null)
                .destinationAccountName(transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getName() : null)
                // --- NUEVO: Mapeo de Etiquetas ---
                .tags(mapTags(transaction.getTags()))
                .build();
    }

    // Helper privado para mantener limpio el código
    private List<TagResponse> mapTags(java.util.Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) return Collections.emptyList();

        return tags.stream()
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .color(tag.getColor())
                        .build())
                .collect(Collectors.toList());
    }
}