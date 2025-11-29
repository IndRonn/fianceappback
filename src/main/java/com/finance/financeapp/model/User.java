package com.finance.financeapp.model;

import com.finance.financeapp.domain.enums.Role; // Asegura este import correcto según tu estructura
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

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    // *** CORRECCIÓN CRÍTICA AQUÍ ***
    // Mapeamos el campo 'password' de Java a la columna 'PASSWORD_HASH' de Oracle.
    @Column(name = "PASSWORD_HASH", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    // insertable=false permite que el DEFAULT CURRENT_TIMESTAMP de Oracle funcione si lo deseas,
    // o puedes manejarlo desde Java con @PrePersist.
    private java.time.LocalDateTime createdAt;

    // --- Implementación de UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

}