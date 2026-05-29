package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.SavingGoalDepositRequest;
import com.smartfinance.smart_finance_hub.dto.request.SavingGoalRequest;
import com.smartfinance.smart_finance_hub.dto.response.SavingGoalResponse;
import com.smartfinance.smart_finance_hub.entity.Fund;
import com.smartfinance.smart_finance_hub.entity.SavingGoal;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.FundType;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.FundRepository;
import com.smartfinance.smart_finance_hub.repository.SavingGoalRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.SavingGoalService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundAccessService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundCategoryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingGoalServiceImpl implements SavingGoalService {

    private final SavingGoalRepository savingGoalRepository;
    private final UserRepository userRepository;
    private final FundRepository fundRepository;
    private final TransactionRepository transactionRepository;
    private final FundAccessService fundAccessService;
    private final FundCategoryService fundCategoryService;

    @Override
    @Transactional
    public SavingGoalResponse createGoal(SavingGoalRequest request, Long userId) {
        User user = requireUser(userId);

        SavingGoal goal = SavingGoal.builder()
                .user(user)
                .name(request.getName().trim())
                .targetAmount(request.getTargetAmount())
                .dueDate(request.getDueDate())
                .status("IN_PROGRESS")
                .build();

        SavingGoal saved = savingGoalRepository.save(goal);
        log.info("Created saving goal: id={}, name={}, userId={}", saved.getId(), saved.getName(), userId);
        return SavingGoalResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingGoalResponse> getMyGoals(Long userId) {
        return savingGoalRepository.findByUserId(userId).stream()
                .map(SavingGoalResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SavingGoalResponse getGoalById(Long goalId, Long userId) {
        SavingGoal goal = requireGoalOwnedBy(goalId, userId);
        return SavingGoalResponse.from(goal);
    }

    @Override
    @Transactional
    public SavingGoalResponse updateGoal(Long goalId, SavingGoalRequest request, Long userId) {
        SavingGoal goal = requireGoalOwnedBy(goalId, userId);
        requireActiveGoal(goal);

        if (request.getName() != null && !request.getName().isBlank()) {
            goal.setName(request.getName().trim());
        }
        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getDueDate() != null) {
            goal.setDueDate(request.getDueDate());
        }

        SavingGoal saved = savingGoalRepository.save(goal);
        return SavingGoalResponse.from(saved);
    }

    @Override
    @Transactional
    public SavingGoalResponse deposit(Long goalId, SavingGoalDepositRequest request, Long userId) {
        SavingGoal goal = requireGoalOwnedBy(goalId, userId);
        requireActiveGoal(goal);
        requirePositiveAmount(request.getAmount());

        Fund fund = requirePersonalFundOwnedBy(request.getFundId(), userId);

        if (fund.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient fund balance for deposit");
        }

        User user = requireUser(userId);

        // Deduct from fund
        var category = fundCategoryService.createFundCategory(user, TransactionType.EXPENSE, "Saving goal deposit");
        Transaction expenseTx = Transaction.builder()
                .user(user)
                .category(category)
                .fund(fund)
                .amount(request.getAmount())
                .type(TransactionType.EXPENSE.name())
                .description("Deposit to saving goal: " + goal.getName())
                .date(LocalDate.now())
                .isApproved(true)
                .status("APPROVED")
                .build();
        transactionRepository.save(expenseTx);
        fund.setBalance(fund.getBalance().subtract(request.getAmount()));
        fundRepository.save(fund);

        // Credit to saving goal
        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("COMPLETED");
        }
        SavingGoal saved = savingGoalRepository.save(goal);

        log.info("Saving goal deposit: goalId={}, amount={}, fundId={}", goalId, request.getAmount(), fund.getId());
        return SavingGoalResponse.from(saved);
    }

    @Override
    @Transactional
    public SavingGoalResponse withdraw(Long goalId, SavingGoalDepositRequest request, Long userId) {
        SavingGoal goal = requireGoalOwnedBy(goalId, userId);
        requirePositiveAmount(request.getAmount());

        if (goal.getCurrentAmount().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient saving goal balance for withdrawal");
        }

        Fund fund = requirePersonalFundOwnedBy(request.getFundId(), userId);
        User user = requireUser(userId);

        // Deduct from saving goal
        goal.setCurrentAmount(goal.getCurrentAmount().subtract(request.getAmount()));
        if ("COMPLETED".equals(goal.getStatus()) && goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus("IN_PROGRESS");
        }
        SavingGoal saved = savingGoalRepository.save(goal);

        // Credit to fund
        var category = fundCategoryService.createFundCategory(user, TransactionType.INCOME, "Saving goal withdrawal");
        Transaction incomeTx = Transaction.builder()
                .user(user)
                .category(category)
                .fund(fund)
                .amount(request.getAmount())
                .type(TransactionType.INCOME.name())
                .description("Withdrawal from saving goal: " + goal.getName())
                .date(LocalDate.now())
                .isApproved(true)
                .status("APPROVED")
                .build();
        transactionRepository.save(incomeTx);
        fund.setBalance(fund.getBalance().add(request.getAmount()));
        fundRepository.save(fund);

        log.info("Saving goal withdraw: goalId={}, amount={}, fundId={}", goalId, request.getAmount(), fund.getId());
        return SavingGoalResponse.from(saved);
    }

    @Override
    @Transactional
    public SavingGoalResponse cancelGoal(Long goalId, Long fundId, Long userId) {
        SavingGoal goal = requireGoalOwnedBy(goalId, userId);

        if ("CANCELLED".equals(goal.getStatus())) {
            throw new IllegalStateException("Saving goal is already cancelled");
        }

        // Return all accumulated amount to the specified fund
        if (goal.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0) {
            Fund fund = requirePersonalFundOwnedBy(fundId, userId);
            User user = requireUser(userId);

            var category = fundCategoryService.createFundCategory(user, TransactionType.INCOME, "Saving goal cancellation");
            Transaction refundTx = Transaction.builder()
                    .user(user)
                    .category(category)
                    .fund(fund)
                    .amount(goal.getCurrentAmount())
                    .type(TransactionType.INCOME.name())
                    .description("Refund from cancelled saving goal: " + goal.getName())
                    .date(LocalDate.now())
                    .isApproved(true)
                    .status("APPROVED")
                    .build();
            transactionRepository.save(refundTx);
            fund.setBalance(fund.getBalance().add(goal.getCurrentAmount()));
            fundRepository.save(fund);
        }

        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setStatus("CANCELLED");
        SavingGoal saved = savingGoalRepository.save(goal);

        log.info("Saving goal cancelled: goalId={}, userId={}", goalId, userId);
        return SavingGoalResponse.from(saved);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private SavingGoal requireGoalOwnedBy(Long goalId, Long userId) {
        SavingGoal goal = savingGoalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Saving goal not found: " + goalId));
        if (!goal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not own this saving goal");
        }
        return goal;
    }

    private void requireActiveGoal(SavingGoal goal) {
        if ("CANCELLED".equals(goal.getStatus()) || "COMPLETED".equals(goal.getStatus())) {
            throw new IllegalStateException("Saving goal is not active: " + goal.getStatus());
        }
    }

    private Fund requirePersonalFundOwnedBy(Long fundId, Long userId) {
        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Fund not found: " + fundId));
        if (!FundType.PERSONAL.name().equals(fund.getFundType())) {
            throw new IllegalArgumentException("Fund must be a personal fund");
        }
        fundAccessService.requireOwner(fundId, userId);
        return fund;
    }

    private void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
    }
}
