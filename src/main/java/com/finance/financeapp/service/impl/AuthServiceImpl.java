package com.finance.financeapp.service.impl;

import com.finance.financeapp.dto.auth.AuthResponse;
import com.finance.financeapp.dto.auth.LoginRequest;
import com.finance.financeapp.dto.auth.RegisterRequest;
import com.finance.financeapp.exception.custom.ConflictException;
import com.finance.financeapp.exception.custom.ResourceNotFoundException;
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
        // HARD MODE: Validación Semántica precisa (409 Conflict)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya está en uso: " + request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado post-autenticación."));

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}