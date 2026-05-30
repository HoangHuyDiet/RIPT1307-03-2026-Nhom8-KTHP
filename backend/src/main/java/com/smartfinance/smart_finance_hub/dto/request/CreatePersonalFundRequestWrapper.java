package com.smartfinance.smart_finance_hub.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePersonalFundRequestWrapper {
    private String name;
    private String icon;
    private String fundType;
    private BigDecimal balance;
    private BigDecimal initialBalance;
    private String currency;
    private String description;
}
