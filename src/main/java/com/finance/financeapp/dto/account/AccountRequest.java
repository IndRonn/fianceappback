package com.finance.financeapp.dto.account;

import com.finance.financeapp.domain.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para crear una nueva cuenta.
 * Validaciones estrictas para evitar datos corruptos.
 */
@Data
public class AccountRequest {

    @NotBlank(message = "El nombre de la cuenta es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @NotNull(message = "El tipo de cuenta es obligatorio (DEBITO/CREDITO)")
    private AccountType type;

    @Size(max = 100)
    private String bankName;

    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.00", inclusive = false, message = "El saldo debe ser positivo.")
    private BigDecimal initialBalance;

    // Opcionales, solo para tarjetas de crédito
    @Min(1) @Max(31)
    private Integer closingDate;

    @Min(1) @Max(31)
    private Integer paymentDate;
}