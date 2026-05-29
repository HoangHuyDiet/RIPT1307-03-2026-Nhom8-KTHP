package com.smartfinance.smart_finance_hub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions",
        indexes = {
            @Index(name = "idx_transactions_fund_status", columnList = "fund_id,status"),
            @Index(name = "idx_transactions_fund_approved_date", columnList = "fund_id,is_approved,date"),
            @Index(name = "idx_transactions_user_fund", columnList = "user_id,fund_id"),
            @Index(name = "idx_transactions_fund_type", columnList = "fund_id,type")
        })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(length = 500)
    private String description;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = false;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "bank_account", length = 100)
    private String bankAccount;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedByUser;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}


