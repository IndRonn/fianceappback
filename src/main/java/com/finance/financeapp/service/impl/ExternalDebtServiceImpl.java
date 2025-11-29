package com.finance.financeapp.service.impl;

import com.finance.financeapp.domain.enums.TransactionType;
import com.finance.financeapp.dto.debt.DebtPaymentRequest;
import com.finance.financeapp.dto.debt.DebtRequest;
import com.finance.financeapp.dto.debt.DebtResponse;
import com.finance.financeapp.dto.transaction.TransactionRequest;
import com.finance.financeapp.exception.custom.BusinessRuleException;
import com.finance.financeapp.exception.custom.ResourceNotFoundException;
import com.finance.financeapp.mapper.ExternalDebtMapper;
import com.finance.financeapp.model.ExternalDebt;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.IExternalDebtRepository;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.service.IExternalDebtService;
import com.finance.financeapp.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExternalDebtServiceImpl implements IExternalDebtService {

    private final IExternalDebtRepository repository;
    private final IUserRepository userRepository;
    private final ExternalDebtMapper mapper;
    private final ITransactionService transactionService; // Reutilización de lógica

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    @Override
    @Transactional
    public DebtResponse createDebt(DebtRequest request) {
        User user = getAuthenticatedUser();
        ExternalDebt debt = mapper.toEntity(request);
        debt.setUser(user);
        return mapper.toResponse(repository.save(debt));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DebtResponse> getMyDebts() {
        User user = getAuthenticatedUser();
        return repository.findByUserId(user.getId()).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional // <--- CRÍTICO PARA INTEGRIDAD HU-18
    public void amortizeDebt(Long debtId, DebtPaymentRequest request) {
        User user = getAuthenticatedUser();

        // 1. Buscar Deuda
        ExternalDebt debt = repository.findById(debtId)
                .filter(d -> d.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada."));

        // 2. Validar que no pagues de más (Hard Mode)
        if (request.getAmount().compareTo(debt.getCurrentBalance()) > 0) {
            throw new BusinessRuleException("El monto a pagar excede la deuda pendiente.");
        }

        // 3. Registrar Transacción de Gasto (Sale dinero de tu cuenta real)
        TransactionRequest trxReq = new TransactionRequest();
        trxReq.setAmount(request.getAmount());
        trxReq.setType(TransactionType.GASTO);
        trxReq.setAccountId(request.getSourceAccountId());
        trxReq.setCategoryId(request.getCategoryId()); // Categoría "Pagos de Deuda"
        trxReq.setTransactionDate(LocalDateTime.now());
        trxReq.setDescription("Amortización: " + debt.getName());

        transactionService.createTransaction(trxReq);

        // 4. Reducir Deuda
        debt.setCurrentBalance(debt.getCurrentBalance().subtract(request.getAmount()));
        repository.save(debt);
    }
}