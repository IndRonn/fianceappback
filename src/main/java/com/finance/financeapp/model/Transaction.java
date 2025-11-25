package com.finance.financeapp.model;

import com.finance.financeapp.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Usamos LocalDateTime para precisión, mapea a DATE/TIMESTAMP

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
    private BigDecimal amount;

    @Column(length = 4000)
    private String description;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDateTime transactionDate;

    // --- RELACIONES ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    private Account account; // Cuenta Origen

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID") // Puede ser NULL en Transferencias
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESTINATION_ACCOUNT_ID") // Solo para Transferencias
    private Account destinationAccount;
}