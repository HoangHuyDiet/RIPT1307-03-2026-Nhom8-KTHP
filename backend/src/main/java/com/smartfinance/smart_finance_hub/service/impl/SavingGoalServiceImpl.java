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
import com.smartfinance.smart_finance_hub.service.NotificationService;
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
    private final NotificationService notificationService;

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

        SavingGoalStatus oldStatus = goal.getStatus();

        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrency(request.getCurrency());
        goal.setDueDate(request.getDueDate());
        goal.setCategory(category);

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingGoalStatus.COMPLETED);
            if (oldStatus != SavingGoalStatus.COMPLETED) {
                try {
                    notificationService.createAndSendNotification(
                            goal.getUser(),
                            "SYSTEM_INFO",
                            null,
                            null,
                            goal.getTargetAmount(),
                            "Chúc mừng bạn đã hoàn thành mục tiêu " + goal.getName() + "! Bạn có thể thực hiện rút tiền hoặc thanh toán ngay.",
                            "Hệ thống",
                            null,
                            null,
                            "MEMBER",
                            "/saving-goals"
                    );
                } catch (Exception e) {
                    log.error("Failed to send goal completion notification", e);
                }
            }
        } else {
            goal.setStatus(SavingGoalStatus.IN_PROGRESS);
        }

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

        PersonalFund fund = null;
        if (request.getPersonalFundId() != null) {
            fund = personalFundRepository.findByIdAndUserId(request.getPersonalFundId(), userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy quỹ cá nhân với ID: " + request.getPersonalFundId()));

            if (fund.getStatus() != FundStatus.ACTIVE) {
                throw new IllegalArgumentException("Quỹ '" + fund.getName() + "' đã bị đóng!");
            }

            if (fund.getBalance().compareTo(request.getAmount()) < 0) {
                throw new IllegalArgumentException(
                        "Số dư quỹ '" + fund.getName() + "' không đủ! Số dư hiện tại: " + fund.getBalance());
            }
        }

        java.math.BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        if (request.getAmount().compareTo(remaining) > 0) {
            throw new IllegalArgumentException(
                    "Số tiền nạp vượt quá mục tiêu! Bạn chỉ có thể nạp tối đa " + remaining + " nữa.");
        }

        if (fund != null) {
            fund.setBalance(fund.getBalance().subtract(request.getAmount()));
            personalFundRepository.save(fund);
        }

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingGoalStatus.COMPLETED);
            try {
                notificationService.createAndSendNotification(
                        goal.getUser(),
                        "SYSTEM_INFO",
                        null,
                        null,
                        goal.getTargetAmount(),
                        "Chúc mừng bạn đã hoàn thành mục tiêu " + goal.getName() + "! Bạn có thể thực hiện rút tiền hoặc thanh toán ngay.",
                        "Hệ thống",
                        null,
                        null,
                        "MEMBER",
                        "/saving-goals"
                );
            } catch (Exception e) {
                log.error("Failed to send goal completion notification", e);
            }
        }

        savingGoalRepository.save(goal);

        String descriptionStr = fund != null
                ? "Nạp tiền vào mục tiêu: " + goal.getName() + " (từ quỹ " + fund.getName() + ")"
                : "Nạp tiền vào mục tiêu: " + goal.getName() + " (từ ngân hàng)";

        Transaction transaction = Transaction.builder()
                .user(goal.getUser())
                .category(goal.getCategory())
                .savingGoal(goal)
                .personalFund(fund)
                .amount(request.getAmount())
                .type("EXPENSE")
                .description(descriptionStr)
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

        PersonalFund fund = null;
        if (request.getPersonalFundId() != null) {
            fund = personalFundRepository.findByIdAndUserId(request.getPersonalFundId(), userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy quỹ cá nhân với ID: " + request.getPersonalFundId()));

            if (fund.getStatus() != FundStatus.ACTIVE) {
                throw new IllegalArgumentException("Quỹ '" + fund.getName() + "' đã bị đóng!");
            }
        }

        goal.setCurrentAmount(goal.getCurrentAmount().subtract(request.getAmount()));

        if (goal.getStatus() == SavingGoalStatus.COMPLETED
                && goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus(SavingGoalStatus.IN_PROGRESS);
        }

        savingGoalRepository.save(goal);

        if (fund != null) {
            fund.setBalance(fund.getBalance().add(request.getAmount()));
            personalFundRepository.save(fund);
        }

        String descriptionStr = fund != null
                ? "Rút tiền từ mục tiêu: " + goal.getName() + " (về quỹ " + fund.getName() + ")"
                : "Rút tiền từ mục tiêu: " + goal.getName() + " (về ngân hàng)";

        Transaction transaction = Transaction.builder()
                .user(goal.getUser())
                .category(goal.getCategory())
                .savingGoal(goal)
                .personalFund(fund)
                .amount(request.getAmount())
                .type("INCOME")
                .description(descriptionStr)
                .bankName(request.getBankName())
                .bankAccount(request.getBankAccount())
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
