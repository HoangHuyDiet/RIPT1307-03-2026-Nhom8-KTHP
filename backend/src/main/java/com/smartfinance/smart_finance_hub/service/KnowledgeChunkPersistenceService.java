package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.entity.KnowledgeDocument;
import com.smartfinance.smart_finance_hub.entity.KnowledgeChunk;
import com.smartfinance.smart_finance_hub.repository.KnowledgeChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgeChunkPersistenceService {
    private final KnowledgeChunkRepository chunkRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearOldChunksInTransaction() {
        chunkRepo.deleteAllInBatch();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveChunkInTransaction(KnowledgeDocument doc, int chunkIndex, String content, String embeddingId) {
        KnowledgeChunk chunk = KnowledgeChunk.builder()
                .document(doc)
                .chunkIndex(chunkIndex)
                .content(content)
                .embeddingId(embeddingId)
                .build();
        chunkRepo.save(chunk);
    }
}
