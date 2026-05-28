package com.smartfinance.smart_finance_hub.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetChartResponse {

    private String month;
    private String type;
    private BigDecimal value;
}


