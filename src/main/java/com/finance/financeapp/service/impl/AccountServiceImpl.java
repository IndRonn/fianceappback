package com.finance.financeapp.service.impl;

import com.finance.financeapp.dto.account.AccountRequest;
import com.finance.financeapp.dto.account.AccountResponse;
import com.finance.financeapp.model.Account;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.IAccountRepository;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.service.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements IAccountService {

    private final IAccountRepository accountRepository;
    private final IUserRepository userRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        // 1. Obtener usuario autenticado del contexto de seguridad
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // 2. Mapear DTO a Entidad
        Account account = Account.builder()
                .name(request.getName())
                .type(request.getType())
                .bankName(request.getBankName())
                .initialBalance(request.getInitialBalance())
                .closingDate(request.getClosingDate())
                .paymentDate(request.getPaymentDate())
                .isActive(true) // Por defecto activa
                .user(user) // Vinculación crítica
                .build();

        // 3. Guardar
        Account savedAccount = accountRepository.save(account);

        // 4. Retornar DTO
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return accountRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper para mapeo (podría usarse MapStruct, pero manual es eficiente aquí)
    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .bankName(account.getBankName())
                .initialBalance(account.getInitialBalance())
                .isActive(account.getIsActive())
                .build();
    }
}