package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.enums.RebuildStatus;
import com.smartfinance.smart_finance_hub.service.KnowledgeIngestionService;
import com.smartfinance.smart_finance_hub.service.KnowledgeRebuildWorker;
import com.smartfinance.smart_finance_hub.service.RagStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service 
@RequiredArgsConstructor 
@Slf4j
public class KnowledgeIngestionServiceImpl implements KnowledgeIngestionService {
    private final KnowledgeRebuildWorker rebuildWorker;
    private final RagStatusService ragStatusService;

    @Override
    public boolean requestRebuild() {
        if (!ragStatusService.startRebuild()) {
            return false;
        }

        try {
            // Chạy bất đồng bộ qua Spring Proxy sang Worker độc lập
            rebuildWorker.rebuildAllAsync();
        } catch (Exception e) {
            log.error("Không thể submit Task lên Thread Pool", e);
            // Giải phóng khóa tránh treo trạng thái RUNNING vĩnh viễn (Lưu ý quan trọng)
            ragStatusService.updateStatus(RebuildStatus.FAILED, "Submit Task failed: " + e.getMessage(), 0);
            throw e;
        }
        return true;
    }
}
