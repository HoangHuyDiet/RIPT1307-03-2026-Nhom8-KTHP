package com.smartfinance.smart_finance_hub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fund_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"fund_id", "user_id"})
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private ShareFund shareFund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fund_role", nullable = false, length = 20)
    @Builder.Default
    private String fundRole = "MEMBER";

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
