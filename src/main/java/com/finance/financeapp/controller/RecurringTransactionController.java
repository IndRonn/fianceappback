package com.finance.financeapp.controller;

import com.finance.financeapp.dto.recurring.RecurringTransactionRequest;
import com.finance.financeapp.dto.recurring.RecurringTransactionResponse;
import com.finance.financeapp.service.IRecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final IRecurringTransactionService service;

    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> create(@Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> getAll() {
        return ResponseEntity.ok(service.getMyRecurringTransactions());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}