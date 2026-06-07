package com.smartfinance.smart_finance_hub.entity;

import com.smartfinance.smart_finance_hub.enums.ChatRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_chat_messages", indexes = {
    @Index(name = "idx_chat_session_created", columnList = "session_id,created_at")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", referencedColumnName = "id", nullable = false)
    private AiChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ChatRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
