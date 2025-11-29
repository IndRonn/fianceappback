package com.finance.financeapp.service.impl;

import com.finance.financeapp.domain.enums.AccountType;
import com.finance.financeapp.dto.transaction.TransactionRequest;
import com.finance.financeapp.dto.transaction.TransactionResponse;
import com.finance.financeapp.exception.custom.BusinessRuleException;
import com.finance.financeapp.exception.custom.ResourceNotFoundException;
import com.finance.financeapp.mapper.TransactionMapper;
import com.finance.financeapp.model.*;
import com.finance.financeapp.repository.*;
import com.finance.financeapp.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements ITransactionService {

    private final ITransactionRepository transactionRepository;
    private final IUserRepository userRepository;
    private final IAccountRepository accountRepository;
    private final ICategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final ITagRepository tagRepository;

    // --- Helpers ---

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado en contexto."));
    }

    private Account findAccountAndVerifyOwnership(Long accountId, Long userId, String typeDesc) {
        return accountRepository.findById(accountId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta " + typeDesc + " no encontrada o acceso denegado."));
    }

    private Transaction findTransactionAndVerifyOwnership(Long trxId, Long userId) {
        return transactionRepository.findById(trxId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada o acceso denegado."));
    }

    private Set<Tag> getTagsFromRequest(List<Long> tagIds, Long userId) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }


        // Convertir List a Set para evitar duplicados en la query
        Set<Long> uniqueIds = new HashSet<>(tagIds);

        List<Tag> foundTags = tagRepository.findAllByIdIn(uniqueIds);

        // Validación estricta: ¿Todas las etiquetas encontradas pertenecen al usuario?
        // Si alguna es de otro usuario, lanzamos error o la filtramos.
        // Hard Mode: Filtramos silenciosamente las ajenas para no dar pistas de seguridad,
        // o lanzamos 404 si no encontramos lo que pidió.

        Set<Tag> validTags = foundTags.stream()
                .filter(t -> t.getUser().getId().equals(userId))
                .collect(Collectors.toSet());

        if (validTags.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("Algunas etiquetas no existen o no te pertenecen");
            // Por ahora, permitimos guardar solo las válidas.
        }

        return validTags;
    }

    private void updateAccountBalance(Account account, BigDecimal amount) {
        if (account.getType() == AccountType.CREDITO) {
            // Lógica para Crédito (Pasivo):
            // El saldo representa DEUDA.
            // - Gasto (amount negativo): Aumenta la deuda (Resta de negativo = Suma).
            // - Pago (amount positivo): Disminuye la deuda (Resta de positivo = Resta).
            account.setInitialBalance(account.getInitialBalance().subtract(amount));
        } else {
            // Lógica para Débito/Efectivo (Activo):
            // El saldo representa DINERO DISPONIBLE.
            // - Gasto (amount negativo): Disminuye el saldo (Suma de negativo = Resta).
            // - Ingreso (amount positivo): Aumenta el saldo.
            account.setInitialBalance(account.getInitialBalance().add(amount));
        }
    }

    // --- CRUD ---

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = getAuthenticatedUser();
        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setUser(user);

        Account sourceAccount = findAccountAndVerifyOwnership(request.getAccountId(), user.getId(), "Origen");
        transaction.setAccount(sourceAccount);

        applyTransactionLogic(transaction, request, sourceAccount, user.getId());

        Set<Tag> tags = getTagsFromRequest(request.getTagIds(), user.getId());
        transaction.setTags(tags);

        applyTransactionLogic(transaction, request, sourceAccount, user.getId());

        accountRepository.save(sourceAccount);
        if (transaction.getDestinationAccount() != null) {
            accountRepository.save(transaction.getDestinationAccount());
        }

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        User user = getAuthenticatedUser();
        Transaction transaction = findTransactionAndVerifyOwnership(id, user.getId());

        revertTransactionBalances(transaction);

        accountRepository.save(transaction.getAccount());
        if (transaction.getDestinationAccount() != null) {
            accountRepository.save(transaction.getDestinationAccount());
        }

        transactionRepository.delete(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        User user = getAuthenticatedUser();
        Transaction existingTrx = findTransactionAndVerifyOwnership(id, user.getId());

        revertTransactionBalances(existingTrx);
        accountRepository.save(existingTrx.getAccount());
        if (existingTrx.getDestinationAccount() != null) {
            accountRepository.save(existingTrx.getDestinationAccount());
        }

        existingTrx.setType(request.getType());
        existingTrx.setAmount(request.getAmount());
        existingTrx.setDescription(request.getDescription());
        existingTrx.setTransactionDate(request.getTransactionDate());
        existingTrx.setExchangeRate(request.getExchangeRate() != null ? request.getExchangeRate() : BigDecimal.ONE);

        Set<Tag> newTags = getTagsFromRequest(request.getTagIds(), user.getId());
        existingTrx.setTags(newTags); // JPA maneja la tabla intermedia automáticamente

        Account newSourceAccount = findAccountAndVerifyOwnership(request.getAccountId(), user.getId(), "Origen");
        existingTrx.setAccount(newSourceAccount);

        existingTrx.setCategory(null);
        existingTrx.setDestinationAccount(null);

        applyTransactionLogic(existingTrx, request, newSourceAccount, user.getId());

        accountRepository.save(newSourceAccount);
        if (existingTrx.getDestinationAccount() != null) {
            accountRepository.save(existingTrx.getDestinationAccount());
        }

        return transactionMapper.toResponse(transactionRepository.save(existingTrx));
    }

    // --- Lógica Financiera ---

    private void applyTransactionLogic(Transaction trx, TransactionRequest req, Account srcAcc, Long userId) {
        switch (trx.getType()) {
            case GASTO:
                // Validaciones específicas de Gasto
                if (req.getCategoryId() == null) {
                    throw new BusinessRuleException("La categoría es obligatoria para gastos.");
                }
                Category catExpense = categoryRepository.findById(req.getCategoryId())
                        .filter(c -> c.getUser().getId().equals(userId))
                        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada."));
                trx.setCategory(catExpense);

                // Gasto: El dinero SALE de la cuenta (-amount)
                updateAccountBalance(srcAcc, req.getAmount().negate());
                break;

            case INGRESO:
                // Validaciones específicas de Ingreso
                if (req.getCategoryId() == null) {
                    throw new BusinessRuleException("La categoría es obligatoria para ingresos.");
                }
                Category catIncome = categoryRepository.findById(req.getCategoryId())
                        .filter(c -> c.getUser().getId().equals(userId))
                        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada."));
                trx.setCategory(catIncome);

                // Ingreso: El dinero ENTRA a la cuenta (+amount)
                updateAccountBalance(srcAcc, req.getAmount());
                break;

            case TRANSFERENCIA:
                // Validaciones específicas de Transferencia
                if (req.getDestinationAccountId() == null) {
                    throw new BusinessRuleException("Cuenta destino obligatoria.");
                }
                if (req.getAccountId().equals(req.getDestinationAccountId())) {
                    throw new BusinessRuleException("Origen y destino no pueden ser iguales.");
                }

                Account destAcc = findAccountAndVerifyOwnership(req.getDestinationAccountId(), userId, "Destino");
                trx.setDestinationAccount(destAcc);

                // 1. RETIRO: Siempre en la moneda de la cuenta ORIGEN (-amount)
                updateAccountBalance(srcAcc, req.getAmount().negate());

                // 2. DEPÓSITO: Cálculo Multi-moneda
                BigDecimal amountToDeposit;

                if (srcAcc.getCurrency().equals(destAcc.getCurrency())) {
                    // Mismo moneda: Transferencia 1 a 1
                    amountToDeposit = req.getAmount();
                } else {
                    // Diferente moneda: Aplicar Tasa de Cambio
                    // Validamos integridad del request
                    if (req.getExchangeRate() == null || req.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BusinessRuleException("Tasa de cambio obligatoria y positiva para transferencias entre monedas distintas.");
                    }

                    // Conversión: Monto Origen * Tasa = Monto Destino
                    amountToDeposit = req.getAmount().multiply(req.getExchangeRate());
                }

                // El dinero ENTRA a la cuenta destino (+amount calculado)
                updateAccountBalance(destAcc, amountToDeposit);
                break;
        }
    }

    private void revertTransactionBalances(Transaction trx) {
        Account srcAcc = trx.getAccount();
        BigDecimal amount = trx.getAmount();

        switch (trx.getType()) {
            case GASTO:
                if (srcAcc.getType() == AccountType.CREDITO) {
                    srcAcc.setInitialBalance(srcAcc.getInitialBalance().subtract(amount));
                } else {
                    srcAcc.setInitialBalance(srcAcc.getInitialBalance().add(amount));
                }
                break;

            case INGRESO:
                if (srcAcc.getType() == AccountType.CREDITO) {
                    srcAcc.setInitialBalance(srcAcc.getInitialBalance().add(amount));
                } else {
                    srcAcc.setInitialBalance(srcAcc.getInitialBalance().subtract(amount));
                }
                break;

            case TRANSFERENCIA:
                if (srcAcc.getType() == AccountType.CREDITO) {
                    srcAcc.setInitialBalance(srcAcc.getInitialBalance().subtract(amount));
                } else {
                    srcAcc.setInitialBalance(srcAcc.getInitialBalance().add(amount));
                }

                Account destAcc = trx.getDestinationAccount();
                if (destAcc != null) {
                    if (destAcc.getType() == AccountType.CREDITO) {
                        destAcc.setInitialBalance(destAcc.getInitialBalance().add(amount));
                    } else {
                        destAcc.setInitialBalance(destAcc.getInitialBalance().subtract(amount));
                    }
                }
                break;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions() {
        User user = getAuthenticatedUser();
        return transactionRepository.findByUserIdOrderByDateDesc(user.getId()).stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }
}