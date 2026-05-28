package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.CreateSavingGoalRequest;
import com.smartfinance.smart_finance_hub.dto.request.GoalTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.PinGoalRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateSavingGoalRequest;
import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.entity.GoalTransaction;
import com.smartfinance.smart_finance_hub.entity.SavingGoal;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.SavingGoalStatus;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.repository.GoalTransactionRepository;
import com.smartfinance.smart_finance_hub.repository.SavingGoalRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.SavingGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingGoalServiceImpl implements SavingGoalService {

    private final SavingGoalRepository savingGoalRepository;
    private final GoalTransactionRepository goalTransactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<SavingGoal> getAllGoals(Long userId) {
        return savingGoalRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    @Transactional
    public SavingGoal createGoal(Long userId, CreateSavingGoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        SavingGoal goal = SavingGoal.builder()
                .user(user)
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .currentAmount(request.getCurrentAmount())
                .currency(request.getCurrency())
                .dueDate(request.getDueDate())
                .category(category)
                .isPinned(false)
                .status(SavingGoalStatus.IN_PROGRESS)
                .build();

        return savingGoalRepository.save(goal);
    }

    @Override
    @Transactional
    public SavingGoal updateGoal(Long userId, Long goalId, UpdateSavingGoalRequest request) {
        SavingGoal goal = savingGoalRepository.findByIdAndUserIdAndDeletedAtIsNull(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Saving goal not found"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrency(request.getCurrency());
        goal.setDueDate(request.getDueDate());
        goal.setCategory(category);

        return savingGoalRepository.save(goal);
    }

    @Override
    @Transactional
    public SavingGoal pinGoal(Long userId, Long goalId, PinGoalRequest request) {
        SavingGoal goal = savingGoalRepository.findByIdAndUserIdAndDeletedAtIsNull(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Saving goal not found"));

        goal.setIsPinned(request.getIsPinned());
        return savingGoalRepository.save(goal);
    }

    @Override
    @Transactional
    public SavingGoal depositGoal(Long userId, Long goalId, GoalTransactionRequest request) {
        SavingGoal goal = savingGoalRepository.findByIdAndUserIdAndDeletedAtIsNull(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Saving goal not found"));

        // Add amount
        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));

        // Check if completed
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingGoalStatus.COMPLETED);
        }

        savingGoalRepository.save(goal);

        // Record transaction
        GoalTransaction transaction = GoalTransaction.builder()
                .savingGoal(goal)
                .amount(request.getAmount())
                .type("DEPOSIT")
                .transactionDate(LocalDateTime.now())
                .build();
        goalTransactionRepository.save(transaction);

        return goal;
    }

    @Override
    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        SavingGoal goal = savingGoalRepository.findByIdAndUserIdAndDeletedAtIsNull(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Saving goal not found"));

        // Soft delete
        goal.setDeletedAt(LocalDateTime.now());
        savingGoalRepository.save(goal);
    }
}
