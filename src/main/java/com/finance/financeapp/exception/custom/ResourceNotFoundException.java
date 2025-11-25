package com.finance.financeapp.exception.custom;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado (Usuario, Cuenta, Categoría, Transacción).
 * Mapea a HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}