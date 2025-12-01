package com.finance.financeapp.controller;

import com.finance.financeapp.dto.bill.BillRequest;
import com.finance.financeapp.dto.bill.BillResponse;
import com.finance.financeapp.service.IServiceBillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class ServiceBillController {

    private final IServiceBillService billService;

    @PostMapping
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.createBill(request));
    }

    @GetMapping
    public ResponseEntity<List<BillResponse>> getMyBills() {
        return ResponseEntity.ok(billService.getMyBills());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BillResponse> updateBill(@PathVariable Long id, @Valid @RequestBody BillRequest request) {
        return ResponseEntity.ok(billService.updateBill(id, request));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> payBill(
            @PathVariable Long id,
            @RequestParam Long accountId
    ) {
        billService.payBill(id, accountId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.noContent().build();
    }
}