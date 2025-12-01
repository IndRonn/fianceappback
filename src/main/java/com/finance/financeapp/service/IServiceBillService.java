package com.finance.financeapp.service;

import com.finance.financeapp.dto.bill.BillRequest;
import com.finance.financeapp.dto.bill.BillResponse;

import java.util.List;

public interface IServiceBillService {

    BillResponse createBill(BillRequest request);

    List<BillResponse> getMyBills();

    BillResponse updateBill(Long id, BillRequest request);

    void payBill(Long billId, Long accountId);

    void deleteBill(Long billId);

}