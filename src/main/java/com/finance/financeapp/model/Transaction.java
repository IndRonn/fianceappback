package com.finance.financeapp.model;

import com.finance.financeapp.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "TRANSACTIONS")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRX_SEQ")
    @SequenceGenerator(name = "TRX_SEQ", sequenceName = "TRX_ID_SEQ", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount; // Monto en moneda de la cuenta origen

    // --- NUEVO: TIPO DE CAMBIO ---
    @Column(name = "EXCHANGE_RATE", nullable = false)
    private BigDecimal exchangeRate; // Default 1.00

    @Column(length = 4000)
    private String description;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESTINATION_ACCOUNT_ID")
    private Account destinationAccount;

    // --- NUEVO: RELACIÓN N:M CON ETIQUETAS ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "TRANSACTION_TAGS",
            joinColumns = @JoinColumn(name = "TRANSACTION_ID"),
            inverseJoinColumns = @JoinColumn(name = "TAG_ID")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();
}