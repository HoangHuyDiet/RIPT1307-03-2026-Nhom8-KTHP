package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.entity.AuditLog;
import com.smartfinance.smart_finance_hub.repository.AuditLogRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String action, String details) {
        try {
            auditLogRepository.save(AuditLog.builder()
                .user(userRepository.getReferenceById(userId))
                .action(action)
                .entityType("AI")
                .details(details)
                .build());
        } catch (Exception e) {
            log.warn("Unable to write AI audit log {} for user {}: {}", action, userId, e.getMessage());
        }
    }
}
