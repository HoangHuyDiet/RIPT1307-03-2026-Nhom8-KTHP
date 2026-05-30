package com.smartfinance.smart_finance_hub.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayBillRequest {
    private String content;
    private BigDecimal amount;
    private Long fundId;
    private Long categoryId;
    private String category;
    private LocalDate date;
    private String type;
}
