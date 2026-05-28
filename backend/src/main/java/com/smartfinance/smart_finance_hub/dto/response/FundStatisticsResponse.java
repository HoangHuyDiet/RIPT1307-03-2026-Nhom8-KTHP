package com.smartfinance.smart_finance_hub.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundStatisticsResponse {

    private Long fundId;
    private String fundName;
    private BigDecimal currentBalance;
    private BigDecimal targetAmount;
    private double progressPercent;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private int totalMembers;
    private int totalTransactions;
    private int approvedTransactions;
    private int pendingTransactions;
    private List<MemberContribution> memberContributions;
    private List<MemberContribution> topContributorsThisMonth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberContribution {
        private Long userId;
        private String displayName;
        private BigDecimal totalContributed;
        private BigDecimal totalSpent;
    }
}


