package com.smartfinance.smart_finance_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsightDTO {

    private String month;
    private String summary;
    private String detailedAdvice;
    private boolean fromCache;
    private String generatedAt;
    private String errorCode;
}
