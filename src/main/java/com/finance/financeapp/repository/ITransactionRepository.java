package com.finance.financeapp.repository;

import com.finance.financeapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdOrderByDateDesc(@Param("userId") Long userId);

    @Query("""
        SELECT COALESCE(SUM(t.amount * t.exchangeRate), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.category.id = :categoryId
          AND t.type = 'GASTO'
          AND t.transactionDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal calculateTotalSpent(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount * t.exchangeRate), 0)
        FROM Transaction t
        JOIN t.category c
        WHERE t.user.id = :userId
          AND t.type = 'GASTO'
          AND c.managementType = 'DIA_A_DIA'
          AND t.transactionDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumTotalVariableExpenses(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.account.id = :accountId
          AND t.type = 'GASTO'
          AND t.transactionDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal getCycleExpenses(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.account.id = :accountId
          AND t.type = 'GASTO'
          AND t.transactionDate <= :cutoffDate
    """)
    BigDecimal getExpensesUpTo(@Param("accountId") Long accountId, @Param("cutoffDate") LocalDateTime cutoffDate);
}