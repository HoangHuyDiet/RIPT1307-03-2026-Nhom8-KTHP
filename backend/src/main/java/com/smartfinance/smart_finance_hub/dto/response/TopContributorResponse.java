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
public class TopContributorResponse {

    private String name;
    private BigDecimal amount;
    private int percent;
    private String avatar;
}


