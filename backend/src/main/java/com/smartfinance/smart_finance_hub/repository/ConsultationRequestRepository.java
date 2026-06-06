package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.ConsultationRequest;
import com.smartfinance.smart_finance_hub.enums.ConsultationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long> {

    Page<ConsultationRequest> findByStatus(ConsultationStatus status, Pageable pageable);

    Page<ConsultationRequest> findByAdvisorId(Long advisorId, Pageable pageable);

    Page<ConsultationRequest> findByAdvisorIdAndStatus(Long advisorId, ConsultationStatus status, Pageable pageable);

    Page<ConsultationRequest> findByUserId(Long userId, Pageable pageable);

    List<ConsultationRequest> findByUserIdAndStatus(Long userId, ConsultationStatus status);

    long countByStatus(ConsultationStatus status);

    long countByAdvisorIdAndStatus(Long advisorId, ConsultationStatus status);
}
