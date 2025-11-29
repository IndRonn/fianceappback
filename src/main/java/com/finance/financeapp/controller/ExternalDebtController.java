package com.finance.financeapp.controller;

import com.finance.financeapp.dto.debt.DebtPaymentRequest;
import com.finance.financeapp.dto.debt.DebtRequest;
import com.finance.financeapp.dto.debt.DebtResponse;
import com.finance.financeapp.service.IExternalDebtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/external-debts")
@RequiredArgsConstructor
public class ExternalDebtController {

    private final IExternalDebtService debtService;

    // [HU-17] Registrar Deuda
    @PostMapping
    public ResponseEntity<DebtResponse> createDebt(@Valid @RequestBody DebtRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(debtService.createDebt(request));
    }

    // Listar mis deudas
    @GetMapping
    public ResponseEntity<List<DebtResponse>> getMyDebts() {
        return ResponseEntity.ok(debtService.getMyDebts());
    }

    // [HU-18] Amortizar (Pagar) Deuda
    // POST /api/v1/external-debts/{id}/amortize
    @PostMapping("/{id}/amortize")
    public ResponseEntity<Void> amortizeDebt(
            @PathVariable Long id,
            @Valid @RequestBody DebtPaymentRequest request
    ) {
        debtService.amortizeDebt(id, request);
        return ResponseEntity.ok().build();
    }
}