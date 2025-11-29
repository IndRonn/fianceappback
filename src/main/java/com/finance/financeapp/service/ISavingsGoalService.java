package com.finance.financeapp.service;

import com.finance.financeapp.dto.goal.SavingsGoalRequest;
import com.finance.financeapp.dto.goal.SavingsGoalResponse;

import java.util.List;

public interface ISavingsGoalService {
    SavingsGoalResponse createGoal(SavingsGoalRequest request);
    List<SavingsGoalResponse> getMyGoals();
}
