package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUserId(Long userId);

    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    List<SupportTicket> findByAssignedAdminId(Long adminId);

    List<SupportTicket> findByStatus(String status);

    List<SupportTicket> findByPriority(String priority);

    List<SupportTicket> findByUserIdAndStatus(Long userId, String status);
}
