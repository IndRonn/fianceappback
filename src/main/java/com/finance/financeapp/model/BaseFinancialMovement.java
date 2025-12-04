package com.finance.financeapp.model;

import com.finance.financeapp.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder // <--- Vital para la herencia del Builder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass // No se convierte en tabla, pero sus campos sí
public abstract class BaseFinancialMovement {

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private TransactionType type;

    @Column(name = "AMOUNT", nullable = false)
    private BigDecimal amount;

    @Column(name = "EXCHANGE_RATE", nullable = false)
    private BigDecimal exchangeRate; // Default 1.00

    @Column(name = "DESCRIPTION", length = 4000)
    private String description;

    // --- RELACIONES COMUNES ---
    // Al definirlas aquí con @JoinColumn, las hijas heredan el mapeo exacto.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;
}