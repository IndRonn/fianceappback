package com.finance.financeapp.dto.transaction;

import com.finance.financeapp.domain.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionRequest {

    @NotNull(message = "El tipo de transacción es obligatorio")
    private TransactionType type;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @Size(max = 4000, message = "La descripción es demasiado larga")
    private String description;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDateTime transactionDate;

    @NotNull(message = "La cuenta de origen es obligatoria")
    private Long accountId;

    // Opcional para transferencias, Obligatorio para Gasto/Ingreso (validado en lógica)
    private Long categoryId;

    // Obligatorio solo para transferencias
    private Long destinationAccountId;

    private BigDecimal exchangeRate;

    private List<Long> tagIds;
}