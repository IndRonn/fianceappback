package com.finance.financeapp.repository;

import com.finance.financeapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Long> {

    // Listar categorías por usuario
    List<Category> findByUserId(Long userId);

    // Opcional: Filtrar por tipo (ej. solo mostrar categorías de GASTO)
    List<Category> findByUserIdAndType(Long userId, com.finance.financeapp.domain.enums.CategoryType type);
}