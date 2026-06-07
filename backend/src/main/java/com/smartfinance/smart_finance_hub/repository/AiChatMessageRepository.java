package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.AiChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    List<AiChatMessage> findByChatSessionIdOrderByCreatedAtAsc(Long sessionId);

    Page<AiChatMessage> findByChatSessionId(Long sessionId, Pageable pageable);

    long countByChatSessionId(Long sessionId);
}
