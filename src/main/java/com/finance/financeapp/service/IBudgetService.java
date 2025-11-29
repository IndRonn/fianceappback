package com.finance.financeapp.service;

import com.finance.financeapp.dto.budget.BudgetRequest;
import com.finance.financeapp.dto.budget.BudgetResponse;

import java.util.List;

public interface IBudgetService {

    /**
     * Crea un nuevo presupuesto para una categoría en un mes específico.
     * Valida que no exista duplicidad (HU-09).
     */
    BudgetResponse createBudget(BudgetRequest request);

    /**
     * Obtiene el dashboard de presupuestos para un mes y año dados.
     * Calcula en tiempo real el progreso y el semáforo (HU-10).
     *
     * @param month Mes (1-12)
     * @param year Año (ej. 2023)
     * @return Lista de presupuestos enriquecida con estado y porcentajes.
     */
    List<BudgetResponse> getBudgets(Integer month, Integer year);
}