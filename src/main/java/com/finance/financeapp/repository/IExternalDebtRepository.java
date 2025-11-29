package com.finance.financeapp.repository;

import com.finance.financeapp.model.ExternalDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IExternalDebtRepository extends JpaRepository<ExternalDebt, Long> {
    List<ExternalDebt> findByUserId(Long userId);
}