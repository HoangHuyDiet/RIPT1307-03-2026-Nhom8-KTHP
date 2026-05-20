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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
