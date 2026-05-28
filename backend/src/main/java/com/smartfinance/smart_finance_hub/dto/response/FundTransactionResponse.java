package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.entity.Transaction;
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
public class FundTransactionResponse {

    private Long transactionId;
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private String type;
    private String description;
    private String categoryName;
    private Boolean isApproved;
    private LocalDate date;
    private LocalDateTime createdAt;

    public static FundTransactionResponse from(Transaction transaction) {
        return FundTransactionResponse.builder()
                .transactionId(transaction.getId())
                .userId(transaction.getUser().getId())
                .userName(transaction.getUser().getDisplayName())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .categoryName(transaction.getCategory().getName())
                .isApproved(transaction.getIsApproved())
                .date(transaction.getDate())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}


