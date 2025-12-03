package com.finance.financeapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    // 1. Inyectamos el valor de la propiedad 'cors.allowed-origins'
    // Este valor será diferente en los perfiles dev y prod.
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permite credenciales (cookies, auth headers)
        config.setAllowCredentials(true);

        // 2. Usamos el valor inyectado: en Dev es "*", en Prod es la URL de Vercel.
        // Convertimos la cadena inyectada a una lista para AllowedOriginPatterns.
        config.setAllowedOriginPatterns(List.of(allowedOrigins));

        // Métodos permitidos (incluyendo OPTIONS, que es vital)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeceras permitidas (incluyendo Authorization para JWT)
        config.setAllowedHeaders(List.of("*"));

        source.registerCorsConfiguration("/**", config); // Aplica a todas las rutas
        return new CorsFilter(source);
    }
}