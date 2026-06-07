package com.smartfinance.smart_finance_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    private String sessionId;
    private String reply;
    private List<AiCitationDTO> citations;
    private boolean aiEnabled;
    private boolean ragAvailable;
    private String errorCode;
    private String disclaimer;
}
