package com.finance.financeapp.service.impl;

import com.finance.financeapp.dto.auth.AuthResponse;
import com.finance.financeapp.dto.auth.LoginRequest;
import com.finance.financeapp.dto.auth.RegisterRequest;
import com.finance.financeapp.model.Role;
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

    // Inyección de dependencias (limpia, usando Lombok)
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Lógica para HU-01: Registro de nuevo usuario.
     */
    @Override
    @Transactional // Buena práctica: si algo falla, se hace rollback
    public AuthResponse register(RegisterRequest request) {

        // 1. Validación de negocio (eficiente, usa 'exists' en lugar de 'find')
        if (userRepository.existsByUsername(request.getEmail())) {
            // (Lanzaremos una excepción personalizada más adelante)
            throw new IllegalArgumentException("El email ya está en uso: " + request.getEmail());
        }

        // 2. Creación de la Entidad (elegante, usando @Builder)
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getEmail()) // Usamos el email como username
                .password(passwordEncoder.encode(request.getPassword())) // Encriptación
                .role(Role.USER) // Rol por defecto
                .build();

        // 3. Persistencia
        userRepository.save(user);

        // 4. Generación de Token
        String jwtToken = jwtService.generateToken(user);

        // 5. Respuesta (DTO)
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Lógica para HU-02: Inicio de sesión.
     */
    @Override
    public AuthResponse login(LoginRequest request) {

        // 1. Autenticación (delegación elegante)
        // Spring Security (AuthenticationManager) hace todo el trabajo pesado.
        // Usará nuestro AuthenticationProvider, que usará nuestro UserDetailsService
        // y nuestro PasswordEncoder para validar las credenciales.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Si la autenticación fue exitosa (no saltó excepción), buscamos al usuario
        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Error interno: Usuario no encontrado post-autenticación."));

        // 3. Generación de Token
        String jwtToken = jwtService.generateToken(user);

        // 4. Respuesta (DTO)
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}