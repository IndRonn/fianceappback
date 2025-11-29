package com.finance.financeapp.service.impl;

import com.finance.financeapp.dto.budget.BudgetRequest;
import com.finance.financeapp.dto.budget.BudgetResponse;
import com.finance.financeapp.exception.custom.BusinessRuleException;
import com.finance.financeapp.exception.custom.ConflictException;
import com.finance.financeapp.exception.custom.ResourceNotFoundException;
import com.finance.financeapp.model.Budget;
import com.finance.financeapp.model.Category;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.IBudgetRepository;
import com.finance.financeapp.repository.ICategoryRepository;
import com.finance.financeapp.repository.ITransactionRepository;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.service.IBudgetService; // ¡Debes crear esta interfaz primero!
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements IBudgetService {

    private final IBudgetRepository budgetRepository;
    private final ITransactionRepository transactionRepository;
    private final ICategoryRepository categoryRepository;
    private final IUserRepository userRepository;

    // --- Helpers ---
    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    // --- Implementación ---

    @Override
    @Transactional
    public BudgetResponse createBudget(BudgetRequest request) {
        User user = getAuthenticatedUser();

        // 1. Validar duplicidad (Un presupuesto por categoría por mes)
        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), request.getCategoryId(), request.getMonth(), request.getYear())) {
            throw new ConflictException("Ya existe un presupuesto para esta categoría en este mes.");
        }

        // 2. Validar Categoría
        Category category = categoryRepository.findById(request.getCategoryId())
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada."));

        // 3. Crear Entidad
        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .amount(request.getLimitAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .build();

        // 4. Guardar y Retornar (con cálculos iniciales en 0)
        return mapToResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(Integer month, Integer year) {
        User user = getAuthenticatedUser();
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(user.getId(), month, year);

        // Transformamos cada presupuesto calculando su gasto real en tiempo real
        return budgets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- Mapeo Inteligente con Lógica de Negocio ---
    private BudgetResponse mapToResponse(Budget budget) {
        // 1. Definir rango de fechas para el mes del presupuesto
        YearMonth yearMonth = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 2. Consultar gasto real en BD (Motor de Cálculo)
        BigDecimal spentAmount = transactionRepository.calculateTotalSpent(
                budget.getUser().getId(),
                budget.getCategory().getId(),
                startOfMonth,
                endOfMonth
        );

        // 3. Cálculos matemáticos
        BigDecimal remaining = budget.getAmount().subtract(spentAmount);

        double percentage = 0.0;
        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = spentAmount.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .doubleValue();
        }

        // 4. Determinar Semáforo (Status)
        String status = "OK"; // Verde
        if (percentage >= 100) {
            status = "DANGER"; // Rojo (Te pasaste)
        } else if (percentage >= 85) {
            status = "WARNING"; // Amarillo (Cuidado)
        }

        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryName(budget.getCategory().getName())
                .limitAmount(budget.getAmount())
                .spentAmount(spentAmount)
                .remainingAmount(remaining)
                .percentage(percentage)
                .status(status)
                .build();
    }
}