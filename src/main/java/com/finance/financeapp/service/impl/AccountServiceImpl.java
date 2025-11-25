package com.finance.financeapp.service.impl;

import com.finance.financeapp.dto.account.AccountRequest;
import com.finance.financeapp.dto.account.AccountResponse;
import com.finance.financeapp.mapper.AccountMapper;
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
    private final AccountMapper accountMapper; // Inyección del Mapper

    // --- Helper Methods ---

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en contexto."));
    }

    private Account findAccountAndVerifyOwnership(Long accountId, Long userId) {
        return accountRepository.findById(accountId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada o acceso denegado."));
    }

    // --- CRUD Implementations ---

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        User user = getAuthenticatedUser();

        // 1. Convertir DTO a Entidad usando MapStruct
        Account account = accountMapper.toEntity(request);

        // 2. Asignar relaciones que el Mapper ignoró
        account.setUser(user);

        // 3. Persistir
        Account savedAccount = accountRepository.save(account);

        // 4. Retornar DTO
        return accountMapper.toResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {
        User user = getAuthenticatedUser();

        return accountRepository.findByUserId(user.getId()).stream()
                .map(accountMapper::toResponse) // Referencia a método: Eficiencia visual
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(Long accountId, AccountRequest request) {
        User user = getAuthenticatedUser();

        // 1. Verificar existencia y propiedad
        Account account = findAccountAndVerifyOwnership(accountId, user.getId());

        // 2. Actualizar la entidad existente con los datos del request
        // MapStruct se encarga de setear name, type, balance, etc.
        accountMapper.updateEntityFromRequest(request, account);

        // 3. Guardar cambios
        Account updatedAccount = accountRepository.save(account);

        return accountMapper.toResponse(updatedAccount);
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        User user = getAuthenticatedUser();
        Account account = findAccountAndVerifyOwnership(accountId, user.getId());
        accountRepository.delete(account);
    }
}