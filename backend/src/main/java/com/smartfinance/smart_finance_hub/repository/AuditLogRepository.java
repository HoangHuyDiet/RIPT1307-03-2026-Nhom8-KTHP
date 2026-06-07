package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByUserEmailOrderByCreatedAtDesc(String email);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByEntityType(String entityType);

   
    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

   
    List<AuditLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
