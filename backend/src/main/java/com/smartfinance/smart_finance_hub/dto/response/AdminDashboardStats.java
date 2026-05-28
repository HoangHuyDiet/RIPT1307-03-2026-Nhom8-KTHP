package com.smartfinance.smart_finance_hub.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStats {

    private long totalUsers;
    private long totalTransactions;
    private long activeUsers;
    private long bannedUsers;
}
