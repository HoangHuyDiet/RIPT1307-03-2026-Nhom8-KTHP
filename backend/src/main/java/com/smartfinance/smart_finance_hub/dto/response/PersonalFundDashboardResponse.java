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
public class PersonalFundDashboardResponse {

    private BigDecimal totalAssets;
    private int totalFunds;
    private BigDecimal totalIncomeThisMonth;
    private BigDecimal totalExpenseThisMonth;
    private List<FundAllocation> allocations;
    private List<BalanceTrend> balanceTrends;
    private List<FeFundActivityResponse> recentActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundAllocation {
        private Long fundId;
        private String fundName;
        private String walletType;
        private BigDecimal balance;
        private double percent;
        private String themeColor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceTrend {
        private String month;
        private BigDecimal totalBalance;
    }
}
