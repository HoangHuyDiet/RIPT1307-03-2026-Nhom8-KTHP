package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.entity.PersonalFund;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalFundResponse {

    private Long id;
    private String name;
    private String fundType;
    private BigDecimal balance;
    private String currency;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
    private String type;
    private String icon;
    private boolean isNegative;

    public static PersonalFundResponse from(PersonalFund fund) {
        String typeVal = "CASH";
        String iconVal = "wallet";
        if (fund.getFundType() != null) {
            switch (fund.getFundType()) {
                case BANK_ACCOUNT -> {
                    typeVal = "BANK";
                    iconVal = "bank";
                }
                case CREDIT_CARD -> {
                    typeVal = "CREDIT_CARD";
                    iconVal = "card";
                }
                case E_WALLET -> {
                    typeVal = "CASH";
                    iconVal = "mobile";
                }
                case INVESTMENT -> {
                    typeVal = "CASH";
                    iconVal = "dollar";
                }
                default -> {
                    typeVal = "CASH";
                    iconVal = "wallet";
                }
            }
        }

        return PersonalFundResponse.builder()
                .id(fund.getId())
                .name(fund.getName())
                .fundType(fund.getFundType() != null ? fund.getFundType().name() : null)
                .balance(fund.getBalance())
                .currency(fund.getCurrency())
                .description(fund.getDescription())
                .status(fund.getStatus() != null ? fund.getStatus().name() : null)
                .createdAt(fund.getCreatedAt())
                .updatedAt(fund.getUpdatedAt())
                .type(typeVal)
                .icon(iconVal)
                .isNegative(fund.getBalance() != null && fund.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .build();
    }
}
