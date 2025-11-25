package com.finance.financeapp.service;

import com.finance.financeapp.dto.account.AccountRequest;
import com.finance.financeapp.dto.account.AccountResponse;

import java.util.List;

public interface IAccountService {

    /**
     * Crea una nueva cuenta asociada al usuario autenticado. (CREATE)
     */
    AccountResponse createAccount(AccountRequest request);

    /**
     * Obtiene todas las cuentas del usuario autenticado. (READ ALL)
     */
    List<AccountResponse> getMyAccounts();

    /**
     * [HU-03 Ext.] Actualiza una cuenta existente del usuario autenticado. (UPDATE)
     */
    AccountResponse updateAccount(Long accountId, AccountRequest request);

    /**
     * [HU-03 Ext.] Elimina una cuenta existente del usuario autenticado. (DELETE)
     */
    void deleteAccount(Long accountId);
}