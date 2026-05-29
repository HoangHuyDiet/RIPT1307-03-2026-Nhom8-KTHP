package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.entity.Fund;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal balance;
    private BigDecimal targetAmount;
    private LocalDate dueDate;
    private String status;
    private String fundType;
    private String walletType;
    private Long createdByUserId;
    private String createdByName;
    private int memberCount;
    private String myRole;
    private LocalDateTime createdAt;

    public static FundResponse from(Fund fund, int memberCount, String myRole) {
        return FundResponse.builder()
                .id(fund.getId())
                .name(fund.getName())
                .description(fund.getDescription())
                .balance(fund.getBalance())
                .targetAmount(fund.getTargetAmount())
                .dueDate(fund.getDueDate())
                .status(fund.getStatus())
                .fundType(fund.getFundType())
                .walletType(fund.getWalletType())
                .createdByUserId(fund.getCreatedByUser().getId())
                .createdByName(fund.getCreatedByUser().getDisplayName())
                .memberCount(memberCount)
                .myRole(myRole)
                .createdAt(fund.getCreatedAt())
                .build();
    }
}


