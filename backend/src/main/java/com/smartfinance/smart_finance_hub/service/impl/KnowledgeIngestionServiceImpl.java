package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.config.condition.RagEnabledCondition;
import com.smartfinance.smart_finance_hub.dto.response.RagStatusDTO;
import com.smartfinance.smart_finance_hub.entity.KnowledgeDocument;
import com.smartfinance.smart_finance_hub.enums.KnowledgeStatus;
import com.smartfinance.smart_finance_hub.enums.RebuildStatus;
import com.smartfinance.smart_finance_hub.repository.KnowledgeChunkRepository;
import com.smartfinance.smart_finance_hub.repository.KnowledgeDocumentRepository;
import com.smartfinance.smart_finance_hub.service.KnowledgeIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Conditional(RagEnabledCondition.class)
@RequiredArgsConstructor
@Slf4j
public class KnowledgeIngestionServiceImpl implements KnowledgeIngestionService {

    private final KnowledgeRebuildWorker rebuildWorker;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;

    private final AtomicBoolean rebuilding = new AtomicBoolean(false);
    private final AtomicReference<RebuildStatus> lastStatus =
        new AtomicReference<>(RebuildStatus.NOT_STARTED);
    private final AtomicReference<LocalDateTime> lastRebuildAt = new AtomicReference<>();
    private final AtomicReference<String> knowledgeBaseHash = new AtomicReference<>("");

    @Override
    public boolean requestRebuild() {
        if (!rebuilding.compareAndSet(false, true)) {
            log.warn("RAG rebuild is already running; rejecting duplicate request");
            return false;
        }

        lastStatus.set(RebuildStatus.RUNNING);
        try {
            rebuildWorker.rebuildAllAsync(this::completeRebuild);
            return true;
        } catch (Exception e) {
            rebuilding.set(false);
            lastStatus.set(RebuildStatus.FAILED);
            knowledgeBaseHash.set("");
            log.error("Unable to submit RAG rebuild task", e);
            throw e;
        }
    }

    @Override
    public RagStatusDTO getStatus() {
        long totalChunks = chunkRepository.count();
        long embeddedChunks = chunkRepository.countByEmbeddingIdIsNotNull();
        boolean available = lastStatus.get() == RebuildStatus.SUCCESS
            && documentRepository.countByStatus(KnowledgeStatus.APPROVED) > 0
            && totalChunks > 0
            && totalChunks == embeddedChunks;

        return RagStatusDTO.builder()
            .ragEnabled(true)
            .ragAvailable(available)
            .rebuildStatus(lastStatus.get())
            .totalDocuments(documentRepository.count())
            .approvedDocuments(documentRepository.countByStatus(KnowledgeStatus.APPROVED))
            .totalChunks(totalChunks)
            .embeddedChunks(embeddedChunks)
            .lastRebuildAt(lastRebuildAt.get() != null ? lastRebuildAt.get().toString() : null)
            .message(buildStatusMessage(available))
            .knowledgeBaseHash(knowledgeBaseHash.get())
            .build();
    }

    private void completeRebuild(boolean success) {
        rebuilding.set(false);
        if (success) {
            lastStatus.set(RebuildStatus.SUCCESS);
            lastRebuildAt.set(LocalDateTime.now());
            knowledgeBaseHash.set(calculateKnowledgeBaseHash());
            log.info("RAG rebuild completed successfully");
        } else {
            lastStatus.set(RebuildStatus.FAILED);
            knowledgeBaseHash.set("");
            log.warn("RAG rebuild failed");
        }
    }

    private String buildStatusMessage(boolean available) {
        if (rebuilding.get()) {
            return "Dang rebuild RAG";
        }
        return available ? "RAG san sang" : "RAG chua san sang";
    }

    private String calculateKnowledgeBaseHash() {
        try {
            List<KnowledgeDocument> docs = documentRepository.findByStatus(KnowledgeStatus.APPROVED);
            docs.sort(Comparator.comparing(KnowledgeDocument::getSourceKey,
                Comparator.nullsFirst(String::compareTo)));

            StringBuilder canonical = new StringBuilder();
            for (KnowledgeDocument doc : docs) {
                canonical.append(nullToEmpty(doc.getSourceKey()))
                    .append(':')
                    .append(sha256(nullToEmpty(doc.getContent())))
                    .append(';');
            }
            return sha256(canonical.toString());
        } catch (Exception e) {
            log.error("Unable to calculate knowledgeBaseHash", e);
            return "";
        }
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash knowledge content", e);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
