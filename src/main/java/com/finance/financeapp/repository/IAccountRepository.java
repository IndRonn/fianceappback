package com.finance.financeapp.repository;

import com.finance.financeapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAccountRepository extends JpaRepository<Account, Long> {

    // Para listar las cuentas de un usuario específico (HU-04)
    List<Account> findByUserId(Long userId);
}