package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.AiChatRequest;
import com.smartfinance.smart_finance_hub.dto.response.AiChatResponse;
import com.smartfinance.smart_finance_hub.dto.response.AiInsightDTO;

public interface AiService {

    AiChatResponse chat(Long userId, AiChatRequest request);

    AiInsightDTO getMonthlyInsight(Long userId, String month, boolean forceRefresh);

    boolean isAiEnabled();
}
