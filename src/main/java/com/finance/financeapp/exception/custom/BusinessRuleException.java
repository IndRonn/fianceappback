package com.finance.financeapp.exception.custom;

/**
 * Excepción para violaciones de reglas de negocio (ej. saldo insuficiente, transferencia a misma cuenta).
 * Mapea a HTTP 400.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}