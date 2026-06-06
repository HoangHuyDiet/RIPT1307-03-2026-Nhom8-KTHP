package com.smartfinance.smart_finance_hub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_chunks", indexes = {
    @Index(name = "idx_chunk_document", columnList = "document_id")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private KnowledgeDocument document;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** ID tham chiếu trong InMemoryEmbeddingStore */
    @Column(name = "embedding_id", length = 100)
    private String embeddingId;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Column(name = "embedding_model", length = 50)
    private String embeddingModel;

    @Column(name = "embedded_at")
    private LocalDateTime embeddedAt;
}
