package com.smartfinance.smart_finance_hub.entity;

import com.smartfinance.smart_finance_hub.enums.ConsultationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_requests")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_question", nullable = false, columnDefinition = "TEXT")
    private String userQuestion;

    /** JSON chuỗi chứa danh sách ConsentScope đã chọn, ví dụ: ["TRANSACTIONS","SAVING_GOALS"] */
    @Column(name = "consent_scope", length = 500)
    private String consentScope;

    /** Snapshot tài chính tại thời điểm tạo yêu cầu (JSON) */
    @Column(name = "financial_snapshot_json", columnDefinition = "TEXT")
    private String financialSnapshotJson;

    /** Bản nháp AI tự động sinh (JSON) — chuyên viên cần kiểm duyệt */
    @Column(name = "ai_draft_json", columnDefinition = "TEXT")
    private String aiDraftJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id")
    private User advisor;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Lời khuyên cuối cùng đã kiểm duyệt của chuyên viên */
    @Column(name = "final_advice", columnDefinition = "TEXT")
    private String finalAdvice;

    /** Optimistic locking — chống race condition khi 2 chuyên viên nhận cùng lúc */
    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
