package com.smartfinance.smart_finance_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetAllocationResponse {

    private Long fundId;
    private String fundName;
    private String fundType;
    private BigDecimal balance;
    private BigDecimal percentage;
}
