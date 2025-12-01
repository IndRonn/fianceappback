package com.finance.financeapp.mapper;

import com.finance.financeapp.dto.account.AccountRequest;
import com.finance.financeapp.dto.account.AccountResponse;
import com.finance.financeapp.model.Account;
import org.springframework.stereotype.Component;

/**
 * Mapper Manual para la entidad Account.
 * Implementación "Hard Mode": Control total sobre la transformación de datos
 * sin dependencias externas como MapStruct.
 */
@Component
public class AccountMapper {

    public Account toEntity(AccountRequest request) {
        if (request == null) return null;

        return Account.builder()
                .name(request.getName())
                .type(request.getType())
                .currency(request.getCurrency()) // Recuerda que ya añadimos esto antes
                .bankName(request.getBankName() != null ? request.getBankName() : "N/A")
                .initialBalance(request.getInitialBalance())
                // --- NUEVO MAPEO ---
                .creditLimit(request.getCreditLimit())
                .closingDate(request.getClosingDate())
                .paymentDate(request.getPaymentDate())
                .isActive(true)
                .build();
    }

    public AccountResponse toResponse(Account account) {
        if (account == null) return null;

        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .currency(account.getCurrency())
                .bankName(account.getBankName())
                .initialBalance(account.getInitialBalance())
                // --- NUEVO MAPEO ---
                .creditLimit(account.getCreditLimit())
                .closingDate(account.getClosingDate())
                .paymentDate(account.getPaymentDate())
                .isActive(account.getIsActive())
                .build();
    }

    public void updateEntityFromRequest(AccountRequest request, Account account) {
        if (request == null || account == null) return;

        account.setName(request.getName());
        account.setType(request.getType());
        account.setCurrency(request.getCurrency());
        if (request.getBankName() != null) account.setBankName(request.getBankName());

        account.setInitialBalance(request.getInitialBalance());

        // --- NUEVO MAPEO ---
        // Permitimos actualizar el límite (ej: el banco te aumentó la línea)
        if (request.getCreditLimit() != null) {
            account.setCreditLimit(request.getCreditLimit());
        }

        account.setClosingDate(request.getClosingDate());
        account.setPaymentDate(request.getPaymentDate());
    }
}