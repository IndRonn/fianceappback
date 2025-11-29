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

    @Override
    @Transactional
    public DebtResponse updateDebt(Long id, DebtRequest request) {
        User user = getAuthenticatedUser();
        ExternalDebt debt = repository.findById(id)
                .filter(d -> d.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada"));

        // 1. Calcular cuánto se ha pagado hasta ahora (Amortizado real)
        // Paid = TotalOriginal - SaldoActual
        BigDecimal amountPaid = debt.getTotalAmount().subtract(debt.getCurrentBalance());

        // 2. Actualizar campos descriptivos
        debt.setName(request.getName());
        debt.setCreditor(request.getCreditor());

        // 3. Actualizar Monto Total y Recalcular Saldo con inteligencia financiera
        if (request.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            debt.setTotalAmount(request.getTotalAmount());
            // El nuevo saldo es el Nuevo Total menos lo que ya pagaste
            // Ejemplo: Debía 100, pagué 20 (Saldo 80).
            // Corrección: Debía 200. Nuevo Saldo = 200 - 20 = 180.
            debt.setCurrentBalance(request.getTotalAmount().subtract(amountPaid));
        }

        // Validación de Seguridad: El saldo no puede ser negativo (no puedes haber pagado más de lo que debes)
        if (debt.getCurrentBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("El nuevo monto total es menor a lo que ya has amortizado.");
        }

        return mapper.toResponse(repository.save(debt));
    }

    @Override
    @Transactional
    public void deleteDebt(Long id) {
        User user = getAuthenticatedUser();
        ExternalDebt debt = repository.findById(id)
                .filter(d -> d.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada"));

        // Nota: Las transacciones de amortización pasadas (GASTOS) quedan en el historial.
        // Solo borramos la ficha de la deuda.
        repository.delete(debt);
    }
}