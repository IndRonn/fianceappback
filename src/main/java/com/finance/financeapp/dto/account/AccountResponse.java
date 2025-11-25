package com.finance.financeapp.dto.account;

import com.finance.financeapp.domain.enums.AccountType;
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
    private Boolean isActive;
    // Eliminado: private Boolean isCash; -> Redundante, el Frontend validará if (type == 'EFECTIVO')
}