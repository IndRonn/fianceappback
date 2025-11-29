package com.finance.financeapp.repository;

import com.finance.financeapp.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBudgetRepository extends JpaRepository<Budget, Long> {

    // Para listar en la pantalla de presupuestos
    List<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);

    // Para validar que no creemos duplicados (BusinessRuleException)
    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);
}
