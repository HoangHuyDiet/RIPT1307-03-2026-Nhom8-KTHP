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
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private String type;
    private String description;
    private LocalDate date;
    private String categoryName;
    private Long categoryId;
    private Boolean isApproved;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .categoryName(transaction.getCategory().getName())
                .categoryId(transaction.getCategory().getId())
                .isApproved(transaction.getIsApproved())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
