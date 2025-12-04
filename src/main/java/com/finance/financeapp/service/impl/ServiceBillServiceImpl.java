package com.finance.financeapp.service.impl;

import com.finance.financeapp.domain.enums.TransactionType;
import com.finance.financeapp.dto.bill.BillRequest;
import com.finance.financeapp.dto.bill.BillResponse;
import com.finance.financeapp.dto.transaction.TransactionRequest;
import com.finance.financeapp.dto.transaction.TransactionResponse;
import com.finance.financeapp.exception.custom.BusinessRuleException;
import com.finance.financeapp.exception.custom.ResourceNotFoundException;
import com.finance.financeapp.mapper.ServiceBillMapper;
import com.finance.financeapp.model.Category;
import com.finance.financeapp.model.ServiceBill;
import com.finance.financeapp.model.Transaction;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.ICategoryRepository;
import com.finance.financeapp.repository.IServiceBillRepository;
import com.finance.financeapp.repository.ITransactionRepository;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.service.IServiceBillService;
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
public class ServiceBillServiceImpl implements IServiceBillService {

    private final IServiceBillRepository billRepository;
    private final IUserRepository userRepository;
    private final ICategoryRepository categoryRepository;
    private final ServiceBillMapper billMapper;

    // Servicios para orquestar el pago
    private final ITransactionService transactionService;
    private final ITransactionRepository transactionRepository;

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    @Override
    @Transactional
    public BillResponse createBill(BillRequest request) {
        User user = getAuthenticatedUser();
        Category category = categoryRepository.findById(request.getCategoryId())
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada."));

        ServiceBill bill = billMapper.toEntity(request);
        bill.setUser(user);
        bill.setCategory(category);
        if (request.getDueDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new BusinessRuleException("La fecha de vencimiento no puede ser anterior a hoy.");
        }
        return billMapper.toResponse(billRepository.save(bill));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getMyBills() {
        User user = getAuthenticatedUser();
        return billRepository.findByUserIdOrderByDueDateAsc(user.getId()).stream()
                .map(billMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BillResponse updateBill(Long id, BillRequest request) {
        User user = getAuthenticatedUser();
        ServiceBill bill = billRepository.findById(id)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Recibo no encontrado."));

        if (bill.getStatus() == ServiceBill.BillStatus.PAGADO) {
            throw new BusinessRuleException("No se puede editar un recibo ya pagado. Elimine el pago primero.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada."));

        billMapper.updateEntity(request, bill);
        bill.setCategory(category);

        return billMapper.toResponse(billRepository.save(bill));
    }

    @Override
    @Transactional
    public void payBill(Long billId, Long accountId) {
        User user = getAuthenticatedUser();

        // 1. Obtener Recibo
        ServiceBill bill = billRepository.findById(billId)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Recibo no encontrado."));

        if (bill.getStatus() == ServiceBill.BillStatus.PAGADO) {
            throw new BusinessRuleException("El recibo ya está pagado.");
        }

        // 2. Crear Transacción de Gasto Automática
        TransactionRequest trxReq = new TransactionRequest();
        trxReq.setAmount(bill.getAmount());
        trxReq.setType(TransactionType.GASTO);
        trxReq.setAccountId(accountId);
        trxReq.setCategoryId(bill.getCategory().getId());
        trxReq.setTransactionDate(LocalDateTime.now());
        trxReq.setDescription("Pago de Servicio: " + bill.getName() + " (" + bill.getCompany() + ")");

        // Asumimos mismo exchange rate por simplicidad operativa.
        // Si hay diferencia de moneda, el monto saldrá nominalmente igual (ej: 10 USD -> 10 PEN),
        // lo cual es aceptable para un MVP o se asume el usuario ajustó el monto en el recibo antes de pagar.
        trxReq.setExchangeRate(BigDecimal.ONE);

        TransactionResponse trxResponse = transactionService.createTransaction(trxReq);

        // 3. Vincular y Actualizar Estado
        Transaction transaction = transactionRepository.findById(trxResponse.getId())
                .orElseThrow(() -> new RuntimeException("Error de integridad: Transacción no encontrada."));

        bill.setTransaction(transaction);
        bill.setStatus(ServiceBill.BillStatus.PAGADO);

        billRepository.save(bill);
    }

    @Override
    @Transactional
    public void deleteBill(Long billId) {
        User user = getAuthenticatedUser();
        ServiceBill bill = billRepository.findById(billId)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Recibo no encontrado."));

        // Regla: Si se borra el recibo, la transacción de pago queda (es historia),
        // pero se rompe el vínculo.
        billRepository.delete(bill);
    }
}