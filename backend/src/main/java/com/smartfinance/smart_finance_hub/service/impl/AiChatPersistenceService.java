package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.entity.AiChatMessage;
import com.smartfinance.smart_finance_hub.entity.AiChatSession;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.ChatRole;
import com.smartfinance.smart_finance_hub.repository.AiChatMessageRepository;
import com.smartfinance.smart_finance_hub.repository.AiChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatPersistenceService {

    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiChatSession getOrCreateSession(String sessionId, User user) {
        return sessionRepository.findBySessionId(sessionId)
            .map(session -> validateOwner(session, user.getId()))
            .orElseGet(() -> createSessionSafely(sessionId, user));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMessage(AiChatSession session, User user, ChatRole role, String content) {
        AiChatMessage message = AiChatMessage.builder()
            .chatSession(session)
            .user(user)
            .role(role)
            .content(content)
            .build();
        messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public boolean isSessionOwnedByUser(String sessionId, Long userId) {
        return sessionRepository.existsBySessionIdAndUserId(sessionId, userId);
    }

    @Transactional(readOnly = true)
    public void validateSessionAccessIfExists(String sessionId, Long userId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        sessionRepository.findBySessionId(sessionId)
            .ifPresent(session -> validateOwner(session, userId));
    }

    private AiChatSession createSessionSafely(String sessionId, User user) {
        try {
            AiChatSession session = AiChatSession.builder()
                .sessionId(sessionId)
                .user(user)
                .build();
            log.info("Created AI chat session {} for user {}", sessionId, user.getId());
            return sessionRepository.save(session);
        } catch (DataIntegrityViolationException ex) {
            AiChatSession existing = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> ex);
            return validateOwner(existing, user.getId());
        }
    }

    private AiChatSession validateOwner(AiChatSession session, Long userId) {
        if (session.getUser() == null || !session.getUser().getId().equals(userId)) {
            throw new SecurityException("Session does not belong to the current user");
        }
        return session;
    }
}
