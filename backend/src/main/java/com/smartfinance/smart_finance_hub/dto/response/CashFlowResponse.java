package com.smartfinance.smart_finance_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowResponse {
    private Integer month;
    private String type;
    private BigDecimal amount;
}
