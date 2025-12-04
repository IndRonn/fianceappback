package com.finance.financeapp.service;

import com.finance.financeapp.dto.recurring.RecurringTransactionRequest;
import com.finance.financeapp.dto.recurring.RecurringTransactionResponse;

import java.util.List;

public interface IRecurringTransactionService {

    RecurringTransactionResponse create(RecurringTransactionRequest request);

    List<RecurringTransactionResponse> getMyRecurringTransactions();

    RecurringTransactionResponse update(Long id, RecurringTransactionRequest request);

    void delete(Long id);

    /**
     * Método invocado automáticamente por el Scheduler.
     * Procesa las transacciones programadas para hoy.
     * (Se expone en la interfaz por si requerimos forzar la ejecución manualmente desde un endpoint Admin).
     */
    void processScheduledTransactions();
}