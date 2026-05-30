package com.smartfinance.smart_finance_hub.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeFundTransactionResponse {

    private Long id;
    private Long fundId;
    private String type;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private String requesterName;
    private String userDisplayName;
    private String categoryName;
    private Boolean isApproved;
    private String status;
    private String bankAccount;
    private String bankName;
    private String rejectReason;
    private Long approvedByUserId;
    private String approvedByName;
}


