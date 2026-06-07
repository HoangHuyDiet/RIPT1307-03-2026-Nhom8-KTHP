package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.ConsultationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessage, Long> {

    List<ConsultationMessage> findByConsultationIdOrderByCreatedAtAsc(Long consultationId);
}
