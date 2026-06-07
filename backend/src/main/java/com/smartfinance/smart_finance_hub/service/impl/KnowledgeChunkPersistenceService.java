package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.entity.KnowledgeChunk;
import com.smartfinance.smart_finance_hub.entity.KnowledgeDocument;
import com.smartfinance.smart_finance_hub.repository.KnowledgeChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service riêng cho các thao tác persistence trên KnowledgeChunk.
 * Dùng REQUIRES_NEW để tránh self-invocation proxy bypass.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeChunkPersistenceService {

    private final KnowledgeChunkRepository chunkRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearAllChunks() {
        log.info("Đang xóa toàn bộ knowledge chunks...");
        chunkRepository.deleteAllChunks();
        log.info("Đã xóa toàn bộ knowledge chunks");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public KnowledgeChunk saveChunk(KnowledgeDocument document, int index,
                                     String content, String embeddingId,
                                     String contentHash, String embeddingModel) {
        KnowledgeChunk chunk = KnowledgeChunk.builder()
            .document(document)
            .chunkIndex(index)
            .content(content)
            .embeddingId(embeddingId)
            .contentHash(contentHash)
            .embeddingModel(embeddingModel)
            .embeddedAt(java.time.LocalDateTime.now())
            .build();
        return chunkRepository.save(chunk);
    }
}
