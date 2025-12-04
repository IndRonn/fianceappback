package com.finance.financeapp.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // <--- Reemplaza a @Builder
@EqualsAndHashCode(callSuper = true) // Buenas prácticas para entidades JPA
@Entity
@Table(name = "TRANSACTIONS")
public class Transaction extends BaseFinancialMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRX_SEQ")
    @SequenceGenerator(name = "TRX_SEQ", sequenceName = "TRX_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESTINATION_ACCOUNT_ID")
    private Account destinationAccount;

    // --- RELACIÓN N:M CON ETIQUETAS ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "TRANSACTION_TAGS",
            joinColumns = @JoinColumn(name = "TRANSACTION_ID"),
            inverseJoinColumns = @JoinColumn(name = "TAG_ID")
    )
    @Builder.Default // Lombok permite esto dentro de SuperBuilder
    private Set<Tag> tags = new HashSet<>();
}