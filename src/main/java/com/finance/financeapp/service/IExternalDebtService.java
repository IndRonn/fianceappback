package com.finance.financeapp.service;

import com.finance.financeapp.dto.debt.DebtPaymentRequest;
import com.finance.financeapp.dto.debt.DebtRequest;
import com.finance.financeapp.dto.debt.DebtResponse;

import java.util.List;


public interface IExternalDebtService {

    DebtResponse createDebt(DebtRequest request);
    List<DebtResponse> getMyDebts();
    void amortizeDebt(Long debtId, DebtPaymentRequest request);
    DebtResponse updateDebt(Long id, DebtRequest request);
    void deleteDebt(Long id);
}
