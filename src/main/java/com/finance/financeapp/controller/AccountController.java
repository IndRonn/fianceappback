package com.finance.financeapp.controller;

import com.finance.financeapp.dto.account.AccountRequest;
import com.finance.financeapp.dto.account.AccountResponse;
import com.finance.financeapp.service.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService accountService;

    // [HU-03] Crear Cuenta (CREATE)
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        // Usamos HttpStatus.CREATED (201) para operaciones POST
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }

    // [HU-04] Obtener Cuentas (READ ALL)
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }

    // [HU-03 Ext] Actualizar Cuenta (UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable("id") Long id,
            @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(id, request));
    }

    // [HU-03 Ext] Eliminar Cuenta (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("id") Long id) {
        accountService.deleteAccount(id);
        // Usamos ResponseEntity.noContent() (204) para indicar éxito sin cuerpo de respuesta
        return ResponseEntity.noContent().build();
    }
}