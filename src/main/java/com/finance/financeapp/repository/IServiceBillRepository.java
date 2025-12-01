package com.finance.financeapp.repository;

import com.finance.financeapp.model.ServiceBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IServiceBillRepository extends JpaRepository<ServiceBill, Long> {
    // Ordenamos por fecha de vencimiento para que lo más urgente salga primero
    List<ServiceBill> findByUserIdOrderByDueDateAsc(Long userId);
}