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
@Table(name = "EXTERNAL_DEBTS")
public class ExternalDebt {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EXT_SEQ")
    @SequenceGenerator(name = "EXT_SEQ", sequenceName = "EXT_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name; // "Préstamo Mamá"

    @Column(length = 100)
    private String creditor; // "Mamá"

    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private BigDecimal totalAmount; // Lo que pediste: 500

    @Column(name = "CURRENT_BALANCE", nullable = false)
    private BigDecimal currentBalance; // Lo que debes hoy: 500

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;
}