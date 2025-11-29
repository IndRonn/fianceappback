package com.finance.financeapp.mapper;

import com.finance.financeapp.dto.debt.DebtRequest;
import com.finance.financeapp.dto.debt.DebtResponse;
import com.finance.financeapp.model.ExternalDebt;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ExternalDebtMapper {

    public ExternalDebt toEntity(DebtRequest request) {
        if (request == null) return null;

        BigDecimal initialBalance = request.getCurrentBalance() != null
                ? request.getCurrentBalance()
                : request.getTotalAmount();

        return ExternalDebt.builder()
                .name(request.getName())
                .creditor(request.getCreditor())
                .totalAmount(request.getTotalAmount())
                .currentBalance(initialBalance)
                .build();
    }

    public DebtResponse toResponse(ExternalDebt debt) {
        if (debt == null) return null;

        BigDecimal paid = debt.getTotalAmount().subtract(debt.getCurrentBalance());
        double percentage = 0.0;
        if (debt.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = paid.divide(debt.getTotalAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return DebtResponse.builder()
                .id(debt.getId())
                .name(debt.getName())
                .creditor(debt.getCreditor())
                .totalAmount(debt.getTotalAmount())
                .currentBalance(debt.getCurrentBalance())
                .progressPercentage(percentage)
                .build();
    }
}