package com.finance.financeapp.service.impl;

import com.finance.financeapp.dto.goal.SavingsGoalRequest;
import com.finance.financeapp.dto.goal.SavingsGoalResponse;
import com.finance.financeapp.exception.custom.ResourceNotFoundException;
import com.finance.financeapp.mapper.SavingsGoalMapper;
import com.finance.financeapp.model.SavingsGoal;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.ISavingsGoalRepository;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.service.ISavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements ISavingsGoalService {

    private final ISavingsGoalRepository repository;
    private final IUserRepository userRepository;
    private final SavingsGoalMapper savingsGoalMapper;
    private final ISavingsGoalRepository savingsGoalRepository;

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    @Override
    @Transactional
    public SavingsGoalResponse createGoal(SavingsGoalRequest request) {
        User user = getAuthenticatedUser();
        SavingsGoal goal = savingsGoalMapper.toEntity(request);
        goal.setUser(user);
        return savingsGoalMapper.toResponse(repository.save(goal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getMyGoals() {
        User user = getAuthenticatedUser();
        return repository.findByUserId(user.getId()).stream()
                .map(savingsGoalMapper::toResponse)
                .collect(Collectors.toList());
    }


    // En SavingsGoalServiceImpl:
    @Override
    @Transactional
    public SavingsGoalResponse updateGoal(Long id, SavingsGoalRequest request) {
        User user = getAuthenticatedUser();
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Meta no encontrada"));

        // Usamos el mapper para actualizar (suponiendo que agregaste updateEntity al mapper)
        // Si no, hazlo manual aquí:
        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        // NO actualizamos currentAmount aquí, eso es sagrado (solo por transacciones)

        return savingsGoalMapper.toResponse(savingsGoalRepository.save(goal));
    }

    @Override
    @Transactional
    public void deleteGoal(Long id) {
        User user = getAuthenticatedUser();
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Meta no encontrada"));

        // Regla de Negocio: ¿Qué pasa con el dinero ahorrado?
        // Opción A (Simple): Se borra la meta y el dinero "desaparece" de la vista (pero sigue en transacciones pasadas).
        // Opción B (Estricta): No dejar borrar si tiene dinero (currentAmount > 0).

        // Implementamos Opción A por simplicidad operativa.
        savingsGoalRepository.delete(goal);
    }
    // Opcional: updateGoal, deleteGoal (Tarea para ti luego)
}