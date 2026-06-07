package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {

    Optional<AiChatSession> findBySessionId(String sessionId);

    Optional<AiChatSession> findBySessionIdAndUserId(String sessionId, Long userId);

    boolean existsBySessionIdAndUserId(String sessionId, Long userId);
}
