package com.finance.financeapp.domain.enums;

/**
 * Tipos de cuenta permitidos en el sistema.
 * Coincide con el CHECK CONSTRAINT de Oracle: IN ('DEBITO', 'CREDITO')
 */
public enum AccountType {
    DEBITO,
    CREDITO,
    EFECTIVO
}