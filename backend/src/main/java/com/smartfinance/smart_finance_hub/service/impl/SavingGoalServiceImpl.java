package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.CreateSavingGoalRequest;
import com.smartfinance.smart_finance_hub.dto.request.GoalTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.PinGoalRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateSavingGoalRequest;
import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.entity.PersonalFund;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.SavingGoal;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.FundStatus;
import com.smartfinance.smart_finance_hub.enums.SavingGoalStatus;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.repository.PersonalFundRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import com.smartfinance.smart_finance_hub.repository.SavingGoalRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.SavingGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingGoalServiceImpl implements SavingGoalService {

    private final SavingGoalRepository savingGoalRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PersonalFundRepository personalFundRepository;

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
                .currentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : java.math.BigDecimal.ZERO)
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
        log.info("depositGoal: userId={}, goalId={}, fundId={}, amount={}",
                userId, goalId, request.getPersonalFundId(), request.getAmount());

        SavingGoal goal = savingGoalRepository.findByIdAndUserIdAndDeletedAtIsNull(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Saving goal not found"));

        PersonalFund fund = personalFundRepository.findByIdAndUserId(request.getPersonalFundId(), userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy quỹ cá nhân với ID: " + request.getPersonalFundId()));

        if (fund.getStatus() != FundStatus.ACTIVE) {
            throw new IllegalArgumentException("Quỹ '" + fund.getName() + "' đã bị đóng!");
        }

        if (fund.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException(
                    "Số dư quỹ '" + fund.getName() + "' không đủ! Số dư hiện tại: " + fund.getBalance());
        }

        fund.setBalance(fund.getBalance().subtract(request.getAmount()));
        personalFundRepository.save(fund);

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingGoalStatus.COMPLETED);
        }

        savingGoalRepository.save(goal);

        Transaction transaction = Transaction.builder()
                .user(goal.getUser())
                .category(goal.getCategory())
                .savingGoal(goal)
                .personalFund(fund)
                .amount(request.getAmount())
                .type("EXPENSE")
                .description("Nạp tiền vào mục tiêu: " + goal.getName() + " (từ quỹ " + fund.getName() + ")")
                .date(java.time.LocalDate.now())
                .isApproved(true)
                .build();
        transactionRepository.save(transaction);

        log.info("depositGoal success: goalId={}, newAmount={}", goalId, goal.getCurrentAmount());
        return goal;
    }

    @Override
    @Transactional
    public SavingGoal withdrawGoal(Long userId, Long goalId, GoalTransactionRequest request) {
        log.info("withdrawGoal: userId={}, goalId={}, fundId={}, amount={}",
                userId, goalId, request.getPersonalFundId(), request.getAmount());

        SavingGoal goal = savingGoalRepository.findByIdAndUserIdAndDeletedAtIsNull(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Saving goal not found"));

        if (goal.getCurrentAmount().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException(
                    "Số dư mục tiêu không đủ để rút! Số dư hiện tại: " + goal.getCurrentAmount());
        }

        PersonalFund fund = personalFundRepository.findByIdAndUserId(request.getPersonalFundId(), userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy quỹ cá nhân với ID: " + request.getPersonalFundId()));

        if (fund.getStatus() != FundStatus.ACTIVE) {
            throw new IllegalArgumentException("Quỹ '" + fund.getName() + "' đã bị đóng!");
        }

        goal.setCurrentAmount(goal.getCurrentAmount().subtract(request.getAmount()));

        if (goal.getStatus() == SavingGoalStatus.COMPLETED
                && goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus(SavingGoalStatus.IN_PROGRESS);
        }

        savingGoalRepository.save(goal);

        fund.setBalance(fund.getBalance().add(request.getAmount()));
        personalFundRepository.save(fund);

        Transaction transaction = Transaction.builder()
                .user(goal.getUser())
                .category(goal.getCategory())
                .savingGoal(goal)
                .personalFund(fund)
                .amount(request.getAmount())
                .type("INCOME")
                .description("Rút tiền từ mục tiêu: " + goal.getName() + " (về quỹ " + fund.getName() + ")")
                .date(java.time.LocalDate.now())
                .isApproved(true)
                .build();
        transactionRepository.save(transaction);

        log.info("withdrawGoal success: goalId={}, remainingAmount={}", goalId, goal.getCurrentAmount());
        return goal;
    }

    @Override
    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        SavingGoal goal = savingGoalRepository.findByIdAndUserIdAndDeletedAtIsNull(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Saving goal not found"));

        goal.setDeletedAt(LocalDateTime.now());
        savingGoalRepository.save(goal);
    }
}
