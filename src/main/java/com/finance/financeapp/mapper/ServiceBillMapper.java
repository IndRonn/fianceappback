package com.finance.financeapp.mapper;

import com.finance.financeapp.dto.bill.BillRequest;
import com.finance.financeapp.dto.bill.BillResponse;
import com.finance.financeapp.model.ServiceBill;
import org.springframework.stereotype.Component;

@Component
public class ServiceBillMapper {

    public ServiceBill toEntity(BillRequest request) {
        if (request == null) return null;

        return ServiceBill.builder()
                .name(request.getName())
                .company(request.getCompany())
                .serviceCode(request.getServiceCode())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .status(ServiceBill.BillStatus.PENDIENTE) // Default al crear
                .build();
    }

    public BillResponse toResponse(ServiceBill bill) {
        if (bill == null) return null;

        return BillResponse.builder()
                .id(bill.getId())
                .name(bill.getName())
                .company(bill.getCompany())
                .serviceCode(bill.getServiceCode())
                .categoryName(bill.getCategory() != null ? bill.getCategory().getName() : null)
                .categoryId(bill.getCategory() != null ? bill.getCategory().getId() : null)
                .currency(bill.getCurrency())
                .amount(bill.getAmount())
                .dueDate(bill.getDueDate())
                .status(bill.getStatus())
                .transactionId(bill.getTransaction() != null ? bill.getTransaction().getId() : null)
                .build();
    }

    public void updateEntity(BillRequest request, ServiceBill bill) {
        if (request == null) return;
        bill.setName(request.getName());
        bill.setCompany(request.getCompany());
        bill.setServiceCode(request.getServiceCode());
        bill.setCurrency(request.getCurrency());
        bill.setAmount(request.getAmount());
        bill.setDueDate(request.getDueDate());
    }
}