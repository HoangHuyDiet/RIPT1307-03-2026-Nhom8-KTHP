package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.CreateSavingGoalRequest;
import com.smartfinance.smart_finance_hub.dto.request.GoalTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.PinGoalRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateSavingGoalRequest;
import com.smartfinance.smart_finance_hub.entity.SavingGoal;

import java.util.List;

public interface SavingGoalService {
    List<SavingGoal> getAllGoals(Long userId);

    SavingGoal createGoal(Long userId, CreateSavingGoalRequest request);

    SavingGoal updateGoal(Long userId, Long goalId, UpdateSavingGoalRequest request);

    SavingGoal pinGoal(Long userId, Long goalId, PinGoalRequest request);

    SavingGoal depositGoal(Long userId, Long goalId, GoalTransactionRequest request);

    void deleteGoal(Long userId, Long goalId);
}
