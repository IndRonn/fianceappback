package com.finance.financeapp.service;

import com.finance.financeapp.dto.account.AccountRequest;
import com.finance.financeapp.dto.account.AccountResponse;

import java.util.List;

public interface IAccountService {

    /**
     * Crea una nueva cuenta asociada al usuario autenticado.
     */
    AccountResponse createAccount(AccountRequest request);

    /**
     * Obtiene todas las cuentas del usuario autenticado.
     */
    List<AccountResponse> getMyAccounts();
}