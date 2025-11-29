package com.finance.financeapp.controller;

import com.finance.financeapp.dto.budget.BudgetRequest;
import com.finance.financeapp.dto.budget.BudgetResponse;
import com.finance.financeapp.service.IBudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final IBudgetService budgetService;

    // [HU-09] Crear Presupuesto
    // POST /api/v1/budgets
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.createBudget(request));
    }

    // [HU-10] Dashboard de Presupuestos (Semáforo)
    // GET /api/v1/budgets?month=11&year=2023
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(
            @RequestParam(name = "month") Integer month,
            @RequestParam(name = "year") Integer year
    ) {
        // Validamos mínimamente los parámetros aquí o confiamos en el Service/BeanValidation
        // "Hard Mode": Si no envían params, Spring lanzará 400 Bad Request automáticamente.
        return ResponseEntity.ok(budgetService.getBudgets(month, year));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}