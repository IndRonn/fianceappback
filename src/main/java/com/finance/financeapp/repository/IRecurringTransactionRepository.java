package com.finance.financeapp.repository;

import com.finance.financeapp.model.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IRecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    /**
     * Busca todas las transacciones recurrentes activas cuya próxima fecha de ejecución
     * es igual o anterior a la fecha actual (hoy).
     */
    List<RecurringTransaction> findByIsActiveTrueAndNextExecutionDateLessThanEqual(LocalDate today);
}