package com.finance.financeapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de seguridad JWT "Blindado".
 * Implementa la estrategia de "Fallo Silencioso" para evitar bloqueos 403
 * en rutas públicas cuando se envían tokens caducados o de usuarios eliminados.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Paso rápido: Si no hay autorización, continuar cadena (vital para /register)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);

            // 2. Validación de Token
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Aquí puede saltar UsernameNotFoundException si limpiaste la BD
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Usuario autenticado exitosamente
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // 3. ESTRATEGIA DE RESILIENCIA:
            // Si algo falla con el token (expirado, usuario borrado, malformado),
            // NO lanzamos error. Limpiamos contexto y dejamos pasar la petición como "Anónima".
            // Si la ruta requiere auth, Spring Security la rechazará más adelante (403).
            // Si la ruta es pública, pasará exitosamente (200).
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}