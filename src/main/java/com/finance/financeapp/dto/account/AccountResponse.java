package com.finance.financeapp.dto.account;

import com.finance.financeapp.domain.enums.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de respuesta.
 * Ocultamos datos sensibles del usuario, solo devolvemos info de la cuenta.
 */
@Data
@Builder
public class AccountResponse {
    private Long id;
    private String name;
    private AccountType type;
    private String bankName;
    private BigDecimal initialBalance;
    private Boolean isActive;
}