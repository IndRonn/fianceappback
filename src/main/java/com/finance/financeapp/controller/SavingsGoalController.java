package com.finance.financeapp.controller;

import com.finance.financeapp.dto.goal.SavingsGoalRequest;
import com.finance.financeapp.dto.goal.SavingsGoalResponse;
import com.finance.financeapp.service.ISavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/savings-goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final ISavingsGoalService service;

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> create(@Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createGoal(request));
    }

    @GetMapping
    public ResponseEntity<List<SavingsGoalResponse>> getAll() {
        return ResponseEntity.ok(service.getMyGoals());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.ok(service.updateGoal(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        service.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }
}