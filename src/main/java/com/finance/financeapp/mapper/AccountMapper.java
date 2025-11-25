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

    /**
     * Transforma un Request (DTO) a una Entidad JPA.
     * Se usa al crear una cuenta nueva.
     */
    public Account toEntity(AccountRequest request) {
        if (request == null) {
            return null;
        }

        return Account.builder()
                // ID y User se ignoran aquí (se manejan en el servicio/BD)
                .name(request.getName())
                .type(request.getType())
                // Lógica de Negocio: Valor por defecto para banco si es nulo
                .bankName(request.getBankName() != null ? request.getBankName() : "N/A")
                .initialBalance(request.getInitialBalance())
                .closingDate(request.getClosingDate())
                .paymentDate(request.getPaymentDate())
                .isActive(true) // Por defecto activa al crear
                .build();
    }

    /**
     * Transforma una Entidad JPA a un Response (DTO).
     * Se usa para devolver datos al frontend.
     */
    public AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType()) // El Enum viaja tal cual
                .bankName(account.getBankName())
                .initialBalance(account.getInitialBalance())
                .isActive(account.getIsActive())
                .build();
    }

    /**
     * Actualiza una entidad existente con los datos del Request.
     * Implementación manual del "Merge".
     */
    public void updateEntityFromRequest(AccountRequest request, Account account) {
        if (request == null || account == null) {
            return;
        }

        // Solo actualizamos los campos permitidos
        account.setName(request.getName());
        account.setType(request.getType());

        // Manejo de nulo para BankName en actualización
        if (request.getBankName() != null) {
            account.setBankName(request.getBankName());
        }

        account.setInitialBalance(request.getInitialBalance());
        account.setClosingDate(request.getClosingDate());
        account.setPaymentDate(request.getPaymentDate());

        // Nota: isActive no se actualiza aquí, suele requerir un endpoint de "soft delete" específico.
    }
}