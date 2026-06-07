package com.smartfinance.smart_finance_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Bản chụp tài chính đầy đủ tại một thời điểm — đầu vào cho prompt AI.
 * Chỉ build phần nào tương ứng với ConsentScope mà user đã đồng ý.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSnapshotDTO {

    private String month;  // "2026-06"
    private Long userId;

    // --- Scope: TRANSACTIONS ---
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSaving;
    private Map<String, BigDecimal> expenseByCategory;
    private Map<String, BigDecimal> incomeByCategory;
    private List<RecurringInfo> upcomingRecurring;

    // --- Scope: SAVING_GOALS ---
    private List<SavingGoalInfo> savingGoals;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecurringInfo {
        private String description;
        private BigDecimal amount;
        private String frequency;
        private String nextRunDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavingGoalInfo {
        private String name;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private Double progressPercent;
        private String dueDate;
        private String status;
    }
}
