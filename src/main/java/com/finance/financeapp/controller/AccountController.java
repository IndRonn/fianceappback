package com.finance.financeapp.controller;

import com.finance.financeapp.dto.account.AccountRequest;
import com.finance.financeapp.dto.account.AccountResponse;
import com.finance.financeapp.service.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.ok(accountService.createAccount(request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }
}