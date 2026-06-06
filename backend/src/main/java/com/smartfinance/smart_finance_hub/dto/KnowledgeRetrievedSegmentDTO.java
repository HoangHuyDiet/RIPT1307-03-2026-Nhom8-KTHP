package com.smartfinance.smart_finance_hub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRetrievedSegmentDTO {
    private String content;
    private String sourceUrl;
    private String title;
    private Double score;
}
