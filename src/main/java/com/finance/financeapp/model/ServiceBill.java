package com.finance.financeapp.model;

import com.finance.financeapp.domain.enums.Frequency;
import com.finance.financeapp.domain.enums.CurrencyType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SERVICE_BILLS")
public class ServiceBill {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BIL_SEQ")
    @SequenceGenerator(name = "BIL_SEQ", sequenceName = "BIL_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name; // Ej: "Luz Casa"

    @Column(length = 100)
    private String company; // Ej: "Hidrandina"

    @Column(name = "SERVICE_CODE", length = 50)
    private String serviceCode; // Ej: "123456"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyType currency;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "DUE_DATE", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus status;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- RELACIONES ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private Category category;

    // Auditoría: El recibo sabe con qué transacción se pagó
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_ID")
    private Transaction transaction;

    public enum BillStatus {
        PENDIENTE,
        PAGADO,
        VENCIDO
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;
}