package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.config.condition.RagEnabledCondition;
import com.smartfinance.smart_finance_hub.entity.KnowledgeDocument;
import com.smartfinance.smart_finance_hub.enums.KnowledgeStatus;
import com.smartfinance.smart_finance_hub.repository.KnowledgeDocumentRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
@Conditional(RagEnabledCondition.class)
@RequiredArgsConstructor
@Slf4j
public class KnowledgeRebuildWorker {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkPersistenceService chunkPersistence;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Value("${ai.rag.chunk-size-chars:1200}")
    private int chunkSizeChars;

    @Value("${ai.rag.chunk-overlap-chars:150}")
    private int chunkOverlapChars;

    @Value("${gemini.embedding-model:gemini-embedding-001}")
    private String embeddingModelName;

    @Async("ragRebuildExecutor")
    public void rebuildAllAsync(Consumer<Boolean> onComplete) {
        boolean success = false;
        log.info("Starting full RAG vector store rebuild");
        try {
            embeddingStore.removeAll();
            chunkPersistence.clearAllChunks();

            List<KnowledgeDocument> documents = documentRepository.findByStatus(KnowledgeStatus.APPROVED);
            int totalChunks = 0;
            for (KnowledgeDocument doc : documents) {
                totalChunks += processDocument(doc);
            }

            success = true;
            log.info("RAG rebuild completed: {} documents, {} chunks", documents.size(), totalChunks);
        } catch (Exception e) {
            log.error("RAG rebuild failed. Cleaning partial vector/chunk data.", e);
            cleanupAfterFailure();
        } finally {
            if (onComplete != null) {
                onComplete.accept(success);
            }
        }
    }

    private int processDocument(KnowledgeDocument doc) {
        String content = doc.getContent();
        if (content == null || content.isBlank()) {
            log.warn("Skipping empty knowledge document {}", doc.getSourceKey());
            return 0;
        }

        List<String> chunks = chunkText(content, chunkSizeChars, chunkOverlapChars);
        int created = 0;
        for (int i = 0; i < chunks.size(); i++) {
            String chunkContent = chunks.get(i);
            String contentHash = sha256(chunkContent);

            TextSegment segment = TextSegment.from(chunkContent,
                Metadata.metadata("sourceKey", doc.getSourceKey())
                    .put("title", doc.getTitle())
                    .put("sourceUrl", doc.getSourceUrl())
                    .put("chunkIndex", String.valueOf(i)));

            Embedding embedding = embeddingModel.embed(segment).content();
            String embeddingId = embeddingStore.add(embedding, segment);
            chunkPersistence.saveChunk(doc, i, chunkContent, embeddingId, contentHash, embeddingModelName);
            created++;
        }

        log.info("Knowledge document '{}' indexed into {} chunks", doc.getTitle(), created);
        return created;
    }

    private List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int step = Math.max(1, chunkSize - overlap);
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += step;
        }
        return chunks;
    }

    private void cleanupAfterFailure() {
        try {
            embeddingStore.removeAll();
            chunkPersistence.clearAllChunks();
        } catch (Exception cleanupError) {
            log.error("Emergency RAG cleanup failed", cleanupError);
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
            throw new IllegalStateException("Unable to hash knowledge chunk", e);
        }
    }
}
