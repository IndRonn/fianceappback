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
 * Filtro que intercepta TODAS las peticiones HTTP (una vez por petición).
 * Se encarga de extraer, validar el token JWT y establecer la autenticación
 * en el SecurityContextHolder.
 */
@Component // Lo marcamos como un Bean de Spring
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Viene de ApplicationConfig

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtener la cabecera 'Authorization'
        final String authHeader = request.getHeader("Authorization");

        // 2. Validar la cabecera
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Petición sin token, se la pasamos al siguiente filtro
            return;
        }

        // 3. Extraer el token (quitando el prefijo "Bearer ")
        final String jwt = authHeader.substring(7);

        // 4. Extraer el username (email) del token
        final String username;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Token inválido (expirado, malformado, etc.)
            filterChain.doFilter(request, response);
            return;
        }


        // 5. Validar el token y que el usuario no esté ya autenticado
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Cargar el usuario desde la BBDD (usando nuestro UserDetailsService)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 7. Validar el token contra los datos del usuario
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 8. ¡Éxito! Crear la autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No usamos credenciales (password) aquí
                        userDetails.getAuthorities()
                );

                // 9. Añadir detalles (IP, etc.) a la autenticación
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 10. Establecer al usuario como AUTENTICADO en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 11. Pasar la petición al siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }
}