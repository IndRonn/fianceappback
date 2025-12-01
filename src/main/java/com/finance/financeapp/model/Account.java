package com.finance.financeapp.model;

import com.finance.financeapp.domain.enums.AccountType;
import com.finance.financeapp.domain.enums.CurrencyType; // <--- Nuevo Import
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ACCOUNTS")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACC_SEQ")
    @SequenceGenerator(name = "ACC_SEQ", sequenceName = "ACC_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    // --- NUEVO: MONEDA ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyType currency;

    @Column(name = "BANK_NAME")
    private String bankName;

    @Column(name = "INITIAL_BALANCE", nullable = false)
    private BigDecimal initialBalance;

    @Column(name = "CREDIT_LIMIT")
    private BigDecimal creditLimit;

    @Column(name = "CLOSING_DATE")
    private Integer closingDate;

    @Column(name = "PAYMENT_DATE")
    private Integer paymentDate;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;
}