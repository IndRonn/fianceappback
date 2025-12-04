package com.finance.financeapp.model;

import com.finance.financeapp.domain.enums.Frequency;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // <--- Corrección crítica
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "RECURRING_TRANSACTIONS")
// Nota: No necesitamos @AttributeOverride porque los nombres de columna
// en la tabla RECURRING_TRANSACTIONS coinciden exactamente con los de la clase base.
public class RecurringTransaction extends BaseFinancialMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REC_TRX_SEQ")
    // CORRECCIÓN: El nombre de la secuencia en tu DDL V2 es REC_TRX_ID_SEQ, no REC_ID_SEQ
    @SequenceGenerator(name = "REC_TRX_SEQ", sequenceName = "REC_TRX_ID_SEQ", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "NEXT_EXECUTION_DATE", nullable = false)
    private LocalDate nextExecutionDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESTINATION_ACCOUNT_ID")
    private Account destinationAccount;
}