package com.finance.financeapp.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO estándar para respuestas de error de la API.
 * Garantiza una estructura JSON uniforme para cualquier fallo en el sistema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp; // Momento exacto del error
    private int status;              // Código HTTP (400, 401, 404, etc.)
    private String error;            // Descripción técnica del estado (ej. "Bad Request")
    private String message;          // Mensaje humano para el usuario
    private String path;             // Endpoint que falló
}