package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.response.FinancialSnapshotDTO;
import com.smartfinance.smart_finance_hub.entity.RecurringSetting;
import com.smartfinance.smart_finance_hub.entity.SavingGoal;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.enums.ConsentScope;
import com.smartfinance.smart_finance_hub.repository.RecurringSettingRepository;
import com.smartfinance.smart_finance_hub.repository.SavingGoalRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import com.smartfinance.smart_finance_hub.service.FinancialSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialSnapshotServiceImpl implements FinancialSnapshotService {

    private final TransactionRepository transactionRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final RecurringSettingRepository recurringSettingRepository;

    @Override
    @Transactional(readOnly = true)
    public FinancialSnapshotDTO buildSnapshot(Long userId, String month, Set<ConsentScope> scopes) {
        FinancialSnapshotDTO.FinancialSnapshotDTOBuilder builder = FinancialSnapshotDTO.builder()
            .userId(userId)
            .month(month);

        if (scopes.contains(ConsentScope.TRANSACTIONS)) {
            populateTransactionData(builder, userId, month);
        }

        if (scopes.contains(ConsentScope.SAVING_GOALS)) {
            populateSavingGoalData(builder, userId);
        }

        return builder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialSnapshotDTO buildFullSnapshot(Long userId, String month) {
        return buildSnapshot(userId, month, EnumSet.allOf(ConsentScope.class));
    }

    private void populateTransactionData(FinancialSnapshotDTO.FinancialSnapshotDTOBuilder builder,
                                          Long userId, String month) {
        YearMonth ym = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
            .findByUserIdAndDateBetween(userId, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();
        Map<String, BigDecimal> incomeByCategory = new LinkedHashMap<>();

        for (Transaction tx : transactions) {
            String categoryName = tx.getCategory() != null ? tx.getCategory().getName() : "Khác";
            BigDecimal amount = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;

            if ("INCOME".equalsIgnoreCase(tx.getType())) {
                totalIncome = totalIncome.add(amount);
                incomeByCategory.merge(categoryName, amount, BigDecimal::add);
            } else if ("EXPENSE".equalsIgnoreCase(tx.getType())) {
                totalExpense = totalExpense.add(amount);
                expenseByCategory.merge(categoryName, amount, BigDecimal::add);
            }
        }

        builder.totalIncome(totalIncome)
            .totalExpense(totalExpense)
            .netSaving(totalIncome.subtract(totalExpense))
            .expenseByCategory(expenseByCategory)
            .incomeByCategory(incomeByCategory);

        List<RecurringSetting> activeRecurring = recurringSettingRepository
            .findByUserIdAndIsActive(userId, true);

        List<FinancialSnapshotDTO.RecurringInfo> recurringInfos = activeRecurring.stream()
            .map(r -> FinancialSnapshotDTO.RecurringInfo.builder()
                .description(r.getDescription())
                .amount(r.getAmount())
                .frequency(r.getFrequency() != null ? r.getFrequency().name() : "MONTHLY")
                .nextRunDate(r.getNextRunDate() != null ? r.getNextRunDate().toString() : null)
                .build())
            .collect(Collectors.toList());

        builder.upcomingRecurring(recurringInfos);
    }

    private void populateSavingGoalData(FinancialSnapshotDTO.FinancialSnapshotDTOBuilder builder,
                                         Long userId) {
        List<SavingGoal> goals = savingGoalRepository.findByUserIdAndDeletedAtIsNull(userId);

        List<FinancialSnapshotDTO.SavingGoalInfo> goalInfos = goals.stream()
            .map(g -> {
                double progress = 0.0;
                if (g.getTargetAmount() != null && g.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                    progress = g.getCurrentAmount()
                        .divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .doubleValue() * 100;
                }

                return FinancialSnapshotDTO.SavingGoalInfo.builder()
                    .name(g.getName())
                    .targetAmount(g.getTargetAmount())
                    .currentAmount(g.getCurrentAmount())
                    .progressPercent(progress)
                    .dueDate(g.getDueDate() != null ? g.getDueDate().toString() : null)
                    .status(g.getStatus() != null ? g.getStatus().name() : "IN_PROGRESS")
                    .build();
            })
            .collect(Collectors.toList());

        builder.savingGoals(goalInfos);
    }
}
