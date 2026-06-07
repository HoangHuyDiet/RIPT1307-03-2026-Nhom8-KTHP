package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySupportTicketIdOrderByCreatedAtAsc(Long supportTicketId);
}
