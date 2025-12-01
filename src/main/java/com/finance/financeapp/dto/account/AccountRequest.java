package com.finance.financeapp.dto.account;

import com.finance.financeapp.domain.enums.AccountType;
import com.finance.financeapp.domain.enums.CurrencyType;
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
    @DecimalMin(value = "0.00", inclusive = true, message = "El saldo debe ser positivo.")
    private BigDecimal initialBalance;

    @DecimalMin(value = "0.01", message = "El límite de crédito debe ser positivo")
    private BigDecimal creditLimit;

    // Opcionales, solo para tarjetas de crédito
    @Min(1) @Max(31)
    private Integer closingDate;

    @Min(1) @Max(31)
    private Integer paymentDate;

    @NotNull(message = "La moneda es obligatoria")
    private CurrencyType currency;
}