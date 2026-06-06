package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.response.RagStatusDTO;

public interface KnowledgeIngestionService {

    /**
     * Kích hoạt rebuild Vector Store async.
     * Trả false nếu đã có rebuild đang chạy (409 Conflict).
     */
    boolean requestRebuild();

    /**
     * Lấy trạng thái RAG hiện tại.
     */
    RagStatusDTO getStatus();
}
