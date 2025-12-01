package com.finance.financeapp.service;

import com.finance.financeapp.dto.account.AccountRequest;
import com.finance.financeapp.dto.account.AccountResponse;

import java.util.List;

public interface IAccountService {

    AccountResponse createAccount(AccountRequest request);

    /**
     * Obtiene todas las cuentas del usuario autenticado.
     */
    List<AccountResponse> getMyAccounts();

    /**
     * Actualiza una cuenta.
     * Permite cambiar nombre, banco, límite, etc.
     */
    AccountResponse updateAccount(Long accountId, AccountRequest request);

    /**
     * Elimina una cuenta (siempre que no rompa integridad referencial crítica).
     */
    void deleteAccount(Long accountId);
}