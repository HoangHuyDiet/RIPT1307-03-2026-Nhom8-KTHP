package com.smartfinance.smart_finance_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundBalanceHistoryResponse {

    private LocalDate date;
    private BigDecimal balance;
}
