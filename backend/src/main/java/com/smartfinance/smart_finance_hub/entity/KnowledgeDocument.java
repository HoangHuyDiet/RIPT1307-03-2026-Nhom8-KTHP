package com.smartfinance.smart_finance_hub.entity;

import com.smartfinance.smart_finance_hub.enums.KnowledgeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_documents", uniqueConstraints = {
    @UniqueConstraint(columnNames = "source_key")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Khóa ổn định để upsert — không thay đổi khi đổi title */
    @Column(name = "source_key", nullable = false, length = 100)
    private String sourceKey;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String category;

    @Column(length = 50)
    private String jurisdiction;

    @Column(length = 10)
    private String language;

    @Column(name = "source_type", length = 50)
    private String sourceType;

    @Column(name = "source_name", length = 255)
    private String sourceName;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private KnowledgeStatus status = KnowledgeStatus.DRAFT;

    @Column(length = 20)
    private String version;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
