package com.finance.financeapp.service;

import com.finance.financeapp.dto.transaction.TransactionRequest;
import com.finance.financeapp.dto.transaction.TransactionResponse;

import java.util.List;

public interface ITransactionService {
    TransactionResponse createTransaction(TransactionRequest request);
    List<TransactionResponse> getMyTransactions();
    TransactionResponse updateTransaction(Long id, TransactionRequest request);
    void deleteTransaction(Long id);
}