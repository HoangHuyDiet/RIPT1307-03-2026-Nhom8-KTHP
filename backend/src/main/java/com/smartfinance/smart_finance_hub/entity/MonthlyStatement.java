package com.smartfinance.smart_finance_hub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_statements", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "month"})
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(nullable = false, length = 7)
    private String month;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "cached_insight", columnDefinition = "TEXT")
    private String cachedInsight;

    @Column(name = "snapshot_hash", length = 64)
    private String snapshotHash;

    @Column(name = "knowledge_base_hash", length = 64)
    private String knowledgeBaseHash;

    @Column(name = "ai_model", length = 80)
    private String aiModel;

    @Column(name = "prompt_version", length = 30)
    private String promptVersion;

    @Column(name = "insight_cached_at")
    private LocalDateTime insightCachedAt;

    @Column(name = "insight_expires_at")
    private LocalDateTime insightExpiresAt;

    @Column(name = "last_ai_refresh_at")
    private LocalDateTime lastAiRefreshAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
