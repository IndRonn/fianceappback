package com.finance.financeapp.service;

import com.finance.financeapp.dto.auth.AuthResponse;
import com.finance.financeapp.dto.auth.LoginRequest;
import com.finance.financeapp.dto.auth.RegisterRequest;

/**
 * Interfaz para el servicio de autenticación.
 * Define las operaciones de registro y login.
 */
public interface IAuthService {

    /**
     * Registra un nuevo usuario en el sistema.
     * @param request DTO con los datos del nuevo usuario.
     * @return AuthResponse con el token JWT.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Autentica un usuario existente.
     * @param request DTO con las credenciales de login.
     * @return AuthResponse con el token JWT.
     */
    AuthResponse login(LoginRequest request);
}