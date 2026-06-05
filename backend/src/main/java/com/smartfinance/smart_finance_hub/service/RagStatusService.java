package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.enums.RebuildStatus;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class RagStatusService {
    private RebuildStatus currentStatus = RebuildStatus.SUCCESS;
    private String knowledgeBaseHash = UUID.randomUUID().toString();

    public synchronized boolean startRebuild() {
        if (currentStatus == RebuildStatus.RUNNING) {
            return false;
        }
        currentStatus = RebuildStatus.RUNNING;
        return true;
    }

    public synchronized void updateStatus(RebuildStatus status, String errorMessage, int totalVectorsAdded) {
        this.currentStatus = status;
        if (status == RebuildStatus.SUCCESS) {
            this.knowledgeBaseHash = UUID.randomUUID().toString();
        }
    }

    public RebuildStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getKnowledgeBaseHash() {
        return knowledgeBaseHash;
    }
}
