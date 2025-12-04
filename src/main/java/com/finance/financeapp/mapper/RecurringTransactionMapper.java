package com.finance.financeapp.mapper;

import com.finance.financeapp.dto.recurring.RecurringTransactionRequest;
import com.finance.financeapp.dto.recurring.RecurringTransactionResponse;
import com.finance.financeapp.model.RecurringTransaction;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionMapper {

    public RecurringTransaction toEntity(RecurringTransactionRequest request) {
        if (request == null) return null;

        return RecurringTransaction.builder()
                .type(request.getType())
                .amount(request.getAmount())
                .description(request.getDescription())
                .exchangeRate(java.math.BigDecimal.ONE) // Default
                // Recurrencia
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                // Al crear, la próxima ejecución es la fecha de inicio
                .nextExecutionDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true) // Activo por defecto
                .build();
    }

    public RecurringTransactionResponse toResponse(RecurringTransaction entity) {
        if (entity == null) return null;

        return RecurringTransactionResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                // Recurrencia
                .frequency(entity.getFrequency())
                .startDate(entity.getStartDate())
                .nextExecutionDate(entity.getNextExecutionDate())
                .endDate(entity.getEndDate())
                .isActive(entity.getIsActive())
                // Relaciones
                .accountId(entity.getAccount().getId())
                .accountName(entity.getAccount().getName())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .destinationAccountId(entity.getDestinationAccount() != null ? entity.getDestinationAccount().getId() : null)
                .destinationAccountName(entity.getDestinationAccount() != null ? entity.getDestinationAccount().getName() : null)
                .build();
    }

    public void updateEntity(RecurringTransactionRequest request, RecurringTransaction entity) {
        if (request == null) return;

        entity.setType(request.getType());
        entity.setAmount(request.getAmount());
        entity.setDescription(request.getDescription());
        entity.setFrequency(request.getFrequency());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());

        // Nota: No actualizamos nextExecutionDate manualmente aquí, eso lo maneja el Scheduler
        // o una lógica específica de re-cálculo si cambia la frecuencia.
    }
}