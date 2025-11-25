package com.finance.financeapp.model;

import com.finance.financeapp.domain.enums.CategoryType;
import com.finance.financeapp.domain.enums.ManagementType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CATEGORIES")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CAT_SEQ")
    @SequenceGenerator(name = "CAT_SEQ", sequenceName = "CAT_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "MANAGEMENT_TYPE", nullable = false)
    private ManagementType managementType;

    // Relación: Muchas categorías pertenecen a un usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;
}