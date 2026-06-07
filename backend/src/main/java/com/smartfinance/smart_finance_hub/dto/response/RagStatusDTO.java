package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.enums.RebuildStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagStatusDTO {

    private boolean ragEnabled;
    private boolean ragAvailable;
    private RebuildStatus rebuildStatus;
    private long totalDocuments;
    private long approvedDocuments;
    private long totalChunks;
    private long embeddedChunks;
    private String lastRebuildAt;
    private String message;
    private String knowledgeBaseHash;
}
