package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.enums.AiErrorCode;
import com.smartfinance.smart_finance_hub.enums.RebuildStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAvailabilityDTO {

    private boolean aiEnabled;
    private boolean aiAvailable;
    private boolean ragEnabled;
    private boolean ragAvailable;
    private RebuildStatus rebuildStatus;
    private String knowledgeBaseHash;
    private boolean proRequired;
    private boolean aiAccessible;
    private List<String> allowedRoles;
    private AiErrorCode errorCode;
    private String reason;
}
