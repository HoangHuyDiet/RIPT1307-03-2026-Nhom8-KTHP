package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.config.condition.RagEnabledCondition;
import com.smartfinance.smart_finance_hub.dto.response.RagStatusDTO;
import com.smartfinance.smart_finance_hub.enums.KnowledgeStatus;
import com.smartfinance.smart_finance_hub.enums.RebuildStatus;
import com.smartfinance.smart_finance_hub.repository.KnowledgeChunkRepository;
import com.smartfinance.smart_finance_hub.repository.KnowledgeDocumentRepository;
import com.smartfinance.smart_finance_hub.service.KnowledgeIngestionService;
import com.smartfinance.smart_finance_hub.service.RagStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Service
@Conditional(RagEnabledCondition.class)
@RequiredArgsConstructor
@Slf4j
public class KnowledgeIngestionServiceImpl implements KnowledgeIngestionService {

    private final KnowledgeRebuildWorker rebuildWorker;
    private final RagStatusService ragStatusService;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;

    @Override
    public boolean requestRebuild() {
        if (!ragStatusService.startRebuild()) {
            return false;
        }

        try {
            rebuildWorker.rebuildAllAsync(success -> ragStatusService.updateStatus(
                success ? RebuildStatus.SUCCESS : RebuildStatus.FAILED,
                success ? "" : "RAG rebuild failed",
                0
            ));
            return true;
        } catch (Exception e) {
            log.error("Unable to submit RAG rebuild task", e);
            ragStatusService.updateStatus(RebuildStatus.FAILED, "Submit task failed: " + e.getMessage(), 0);
            throw e;
        }
    }

    @Override
    public RagStatusDTO getStatus() {
        long totalChunks = chunkRepository.count();
        long embeddedChunks = chunkRepository.countByEmbeddingIdIsNotNull();
        long approvedDocuments = documentRepository.countByStatus(KnowledgeStatus.APPROVED);
        RebuildStatus status = ragStatusService.getCurrentStatus();
        boolean available = status == RebuildStatus.SUCCESS
            && approvedDocuments > 0
            && totalChunks > 0
            && totalChunks == embeddedChunks;

        return RagStatusDTO.builder()
            .ragEnabled(true)
            .ragAvailable(available)
            .rebuildStatus(status)
            .totalDocuments(documentRepository.count())
            .approvedDocuments(approvedDocuments)
            .totalChunks(totalChunks)
            .embeddedChunks(embeddedChunks)
            .lastRebuildAt(null)
            .message(buildStatusMessage(status, available))
            .knowledgeBaseHash(ragStatusService.getKnowledgeBaseHash())
            .build();
    }

    private String buildStatusMessage(RebuildStatus status, boolean available) {
        if (status == RebuildStatus.RUNNING) {
            return "Dang rebuild RAG";
        }
        if (status == RebuildStatus.FAILED) {
            return "RAG rebuild that bai";
        }
        return available ? "RAG san sang" : "RAG chua san sang";
    }
}
