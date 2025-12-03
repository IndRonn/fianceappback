package com.finance.financeapp.dto.account;

import com.finance.financeapp.domain.enums.AccountType;
import com.finance.financeapp.domain.enums.CurrencyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountResponse {
    private Long id;
    private String name;
    private AccountType type;
    private String bankName;
    private BigDecimal initialBalance;
    private BigDecimal creditLimit;
    private Boolean isActive;
    private CurrencyType currency;

    // --- CAMPOS DE FECHAS (Vitales para TC) ---
    private Integer closingDate; // Día de cierre (1-31)
    private Integer paymentDate; // Día de pago (1-31)

    // Deuda Facturada (A pagar ya / Ciclo Cerrado o Anterior)
    private BigDecimal statementBalance;

    // [NUEVO] Deuda del Ciclo Actual (Lo que estás comprando ahora, para el próx. mes)
    private BigDecimal currentCycleBalance;
}