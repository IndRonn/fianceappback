package com.finance.financeapp.dto.bill;

import com.finance.financeapp.domain.enums.BillFrequency;
import com.finance.financeapp.domain.enums.CurrencyType;
import com.finance.financeapp.model.ServiceBill.BillStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class BillResponse {
    private Long id;
    private String name;
    private String company;
    private String serviceCode;
    private String categoryName;
    private Long categoryId; // Útil para el clonado en frontend
    private CurrencyType currency;
    private BigDecimal amount;
    private LocalDate dueDate;
    private BillStatus status;
    private Long transactionId; // Link a la transacción de pago
    private BillFrequency frequency;
}