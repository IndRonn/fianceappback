package com.finance.financeapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permite credenciales (cookies, auth headers)
        config.setAllowCredentials(true);

        // *** IMPORTANTE: AJUSTAR EN PRODUCCIÓN ***
        // Para desarrollo, permitimos todos los orígenes
        config.setAllowedOriginPatterns(List.of("*"));

        // Métodos permitidos (incluyendo OPTIONS, que es vital)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeceras permitidas (incluyendo Authorization para JWT)
        config.setAllowedHeaders(List.of("*"));

        source.registerCorsConfiguration("/**", config); // Aplica a todas las rutas
        return new CorsFilter(source);
    }
}