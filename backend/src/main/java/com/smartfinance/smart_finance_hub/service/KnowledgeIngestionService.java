package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.response.RagStatusDTO;

public interface KnowledgeIngestionService {

    boolean requestRebuild();

    RagStatusDTO getStatus();
}
