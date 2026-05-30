package com.smartfinance.smart_finance_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeFundStatResponse {

    private String title;
    private String value;
    private String trend;
    private String icon;
}


