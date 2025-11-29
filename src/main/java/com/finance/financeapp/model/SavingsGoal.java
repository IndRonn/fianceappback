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
@Table(name = "SAVINGS_GOALS")
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SVG_SEQ")
    @SequenceGenerator(name = "SVG_SEQ", sequenceName = "SVG_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "TARGET_AMOUNT")
    private BigDecimal targetAmount;

    @Column(name = "CURRENT_AMOUNT", nullable = false)
    private BigDecimal currentAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;
}