package com.finance.financeapp.exception.custom;

/**
 * Excepción para conflictos de estado en la base de datos.
 * Típicamente violaciones de unicidad (ej. Email ya registrado).
 * Mapea a HTTP 409.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}