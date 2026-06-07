package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.KnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    List<KnowledgeChunk> findByDocumentIdOrderByChunkIndexAsc(Long documentId);

    @Modifying
    @Query("DELETE FROM KnowledgeChunk kc WHERE kc.document.id = :documentId")
    void deleteByDocumentId(Long documentId);

    @Modifying
    @Query("DELETE FROM KnowledgeChunk kc")
    void deleteAllChunks();

    long countByEmbeddingIdIsNotNull();
}
