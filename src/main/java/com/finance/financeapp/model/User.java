package com.finance.financeapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USERS", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"})
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_SEQ")
    @SequenceGenerator(name = "USER_SEQ", sequenceName = "USER_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    // Usamos 'username' para el login, aunque sea el mismo email
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- Implementación de UserDetails (Spring Security) ---
    // Estas son las columnas "virtuales" que Spring Security necesita.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Devuelve el rol del usuario como una autoridad para Spring Security
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        // Spring Security usará el 'username' (que hemos seteado como el email)
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // No manejamos expiración de cuentas
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // No manejamos bloqueo de cuentas
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // No manejamos expiración de credenciales
    }

    @Override
    public boolean isEnabled() {
        return true; // Cuentas habilitadas por defecto
    }
}