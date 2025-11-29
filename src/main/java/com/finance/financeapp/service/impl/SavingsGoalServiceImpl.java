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
    private final SavingsGoalMapper mapper;

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
    }

    @Override
    @Transactional
    public SavingsGoalResponse createGoal(SavingsGoalRequest request) {
        User user = getAuthenticatedUser();
        SavingsGoal goal = mapper.toEntity(request);
        goal.setUser(user);
        return mapper.toResponse(repository.save(goal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getMyGoals() {
        User user = getAuthenticatedUser();
        return repository.findByUserId(user.getId()).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // Opcional: updateGoal, deleteGoal (Tarea para ti luego)
}