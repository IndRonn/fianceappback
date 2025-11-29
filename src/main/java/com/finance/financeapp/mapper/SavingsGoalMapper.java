package com.finance.financeapp.mapper;

import com.finance.financeapp.dto.goal.SavingsGoalRequest;
import com.finance.financeapp.dto.goal.SavingsGoalResponse;
import com.finance.financeapp.model.SavingsGoal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SavingsGoalMapper {

    public SavingsGoal toEntity(SavingsGoalRequest request) {
        if (request == null) return null;

        return SavingsGoal.builder()
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .currentAmount(request.getInitialAmount() != null ? request.getInitialAmount() : BigDecimal.ZERO)
                .build();
    }

    public SavingsGoalResponse toResponse(SavingsGoal goal) {
        if (goal == null) return null;

        double percentage = 0.0;
        if (goal.getTargetAmount() != null && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = goal.getCurrentAmount()
                    .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        // Capar al 100% visualmente aunque tengas más
        if (percentage > 100.0) percentage = 100.0;

        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .name(goal.getName())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .percentage(percentage)
                .build();
    }

    public void updateEntity(SavingsGoalRequest request, SavingsGoal goal) {
        if (request == null) return;

        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        // Nota: No actualizamos currentAmount aquí, eso solo cambia por transacciones o cierre de caja
    }
}