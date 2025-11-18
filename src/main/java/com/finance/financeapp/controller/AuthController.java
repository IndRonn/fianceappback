package com.finance.financeapp.controller;

import com.finance.financeapp.dto.auth.AuthResponse;
import com.finance.financeapp.dto.auth.LoginRequest;
import com.finance.financeapp.dto.auth.RegisterRequest;
import com.finance.financeapp.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la autenticación (HU-01 y HU-02).
 * Expone los endpoints de registro (/register) y login (/login).
 */
@RestController
@RequestMapping("/api/v1/auth") // Define el prefijo base para todos los endpoints de esta clase
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /**
     * Endpoint para HU-01: Registro de nuevo usuario.
     * HTTP POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        // Delega 100% de la lógica al servicio
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Endpoint para HU-02: Inicio de sesión.
     * HTTP POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        // Delega 100% de la lógica al servicio
        return ResponseEntity.ok(authService.login(request));
    }
}