package com.finance.financeapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "BUDGETS")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BUD_SEQ")
    @SequenceGenerator(name = "BUD_SEQ", sequenceName = "BUD_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount; // El límite establecido

    @Column(nullable = false)
    private Integer month; // 1-12

    @Column(nullable = false)
    private Integer year;

    // Relaciones (FetchType.LAZY es vital para el rendimiento)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private Category category;
}