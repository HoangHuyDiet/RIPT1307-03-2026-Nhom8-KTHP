package com.smartfinance.smart_finance_hub.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {
    private BigDecimal amount;
    private String description;
    private Long categoryId;
    private LocalDate date;
}
