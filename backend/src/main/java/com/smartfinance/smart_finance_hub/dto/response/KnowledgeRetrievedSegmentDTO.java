package com.smartfinance.smart_finance_hub.dto.response;

import dev.langchain4j.data.segment.TextSegment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRetrievedSegmentDTO {

    private TextSegment segment;
    private String text;
    private String title;
    private String sourceKey;
    private String sourceUrl;
    private Double score;
}
