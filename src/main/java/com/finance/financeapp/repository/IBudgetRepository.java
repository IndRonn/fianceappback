package com.finance.financeapp.repository;

import com.finance.financeapp.domain.enums.ManagementType;
import com.finance.financeapp.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IBudgetRepository extends JpaRepository<Budget, Long> {

    // Para listar todos los presupuestos de un mes (Pantalla HU-10)
    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);

    // Para buscar un presupuesto específico (Validaciones)
    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);

    // Validación rápida de existencia (BusinessRuleException)
    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.month = :month AND b.year = :year AND b.category.managementType = :type")
    List<Budget> findByUserIdAndMonthAndYearAndType(
            @Param("userId") Long userId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("type") ManagementType type
    );
}