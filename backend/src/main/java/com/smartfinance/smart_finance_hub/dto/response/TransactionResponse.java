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
    private Long savingGoalId;
    private String savingGoalName;
    private Long personalFundId;
    private String personalFundName;
    private Long linkedTransactionId;

    
    private String category;
    private String fund;

    public static TransactionResponse from(Transaction transaction) {
        String categoryVal = transaction.getCategory() != null ? transaction.getCategory().getName() : null;
        String fundVal = transaction.getPersonalFund() != null ? transaction.getPersonalFund().getName() : 
                         (transaction.getShareFund() != null ? transaction.getShareFund().getName() + " (Quỹ nhóm)" : null);

        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .categoryName(categoryVal)
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .isApproved(transaction.getIsApproved())
                .createdAt(transaction.getCreatedAt())
                .savingGoalId(transaction.getSavingGoal() != null ? transaction.getSavingGoal().getId() : null)
                .savingGoalName(transaction.getSavingGoal() != null ? transaction.getSavingGoal().getName() : null)
                .personalFundId(transaction.getPersonalFund() != null ? transaction.getPersonalFund().getId() : null)
                .personalFundName(fundVal)
                .linkedTransactionId(transaction.getLinkedTransactionId())
                .category(categoryVal)
                .fund(fundVal)
                .build();
    }
}
