package com.finance.financeapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para la generación y validación de JSON Web Tokens (JWT).
 * Actualizado a la API jjwt 0.12.x (sin deprecaciones).
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration.ms}")
    private long EXPIRATION_TIME;

    /**
     * Genera un token JWT para el usuario.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un token JWT con claims extra.
     * (ACTUALIZADO: .signWith(Key) infiere el algoritmo)
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims) // .setClaims() está obsoleto
                .subject(userDetails.getUsername()) // .setSubject()
                .issuedAt(new Date(System.currentTimeMillis())) // .setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // .setExpiration()
                .signWith(getSignInKey()) // API Moderna: No requiere SignatureAlgorithm
                .compact();
    }

    /**
     * Valida un token JWT.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae el 'subject' (username/email) del token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Método genérico para extraer cualquier 'claim' del token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * (ACTUALIZADO: Usando la API 0.12.x sin deprecaciones)
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey()) // API Moderna: reemplaza .setSigningKey()
                .build()
                .parseSignedClaims(token) // API Moderna: reemplaza .parseClaimsJws()
                .getPayload(); // API Moderna: reemplaza .getBody()
    }

    /**
     * Obtiene la clave de firma (SecretKey) a partir del 'secret' en Base64.
     * (ACTUALIZADO: Devuelve SecretKey en lugar de Key para mejor type-safety)
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        // Keys.hmacShaKeyFor es el método correcto para crear una SecretKey
        // para algoritmos HMAC-SHA (HS256, HS384, HS512)
        return Keys.hmacShaKeyFor(keyBytes);
    }
}