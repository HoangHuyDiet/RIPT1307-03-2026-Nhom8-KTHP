package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.SavingGoalDepositRequest;
import com.smartfinance.smart_finance_hub.dto.request.SavingGoalRequest;
import com.smartfinance.smart_finance_hub.dto.response.SavingGoalResponse;
import java.util.List;

public interface SavingGoalService {

    SavingGoalResponse createGoal(SavingGoalRequest request, Long userId);

    List<SavingGoalResponse> getMyGoals(Long userId);

    SavingGoalResponse getGoalById(Long goalId, Long userId);

    SavingGoalResponse updateGoal(Long goalId, SavingGoalRequest request, Long userId);

    SavingGoalResponse deposit(Long goalId, SavingGoalDepositRequest request, Long userId);

    SavingGoalResponse withdraw(Long goalId, SavingGoalDepositRequest request, Long userId);

    SavingGoalResponse cancelGoal(Long goalId, Long fundId, Long userId);
}
