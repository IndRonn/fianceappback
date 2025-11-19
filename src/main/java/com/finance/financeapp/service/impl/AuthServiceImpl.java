package com.finance.financeapp.service.impl;

import com.finance.financeapp.dto.auth.AuthResponse;
import com.finance.financeapp.dto.auth.LoginRequest;
import com.finance.financeapp.dto.auth.RegisterRequest;
import com.finance.financeapp.domain.enums.Role;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.security.JwtService;
import com.finance.financeapp.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // 1. Validación de Negocio
        // Usamos existsByEmail para mayor claridad semántica, aunque username=email.
        if (userRepository.existsByEmail(request.getEmail())) {
            // Esta excepción será capturada por GlobalExceptionHandler -> 400 Bad Request
            throw new IllegalArgumentException("El email ya está en uso: " + request.getEmail());
        }

        // 2. Construcción con Builder (Patrón de Diseño Creacional)
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // Obligatorio para Oracle (Not Null)
                .build();

        // 3. Persistencia
        userRepository.save(user);

        // 4. Generación de Token
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Spring Security maneja la validación de credenciales internamente.
        // Si falla, lanza BadCredentialsException -> GlobalExceptionHandler -> 401 Unauthorized
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado post-autenticación."));

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}