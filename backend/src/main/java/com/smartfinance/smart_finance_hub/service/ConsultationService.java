package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.ConsultationCreateRequest;
import com.smartfinance.smart_finance_hub.dto.response.ConsultationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConsultationService {

    ConsultationDTO createRequest(Long userId, ConsultationCreateRequest request);

    Page<ConsultationDTO> getUserRequests(Long userId, Pageable pageable);

    Page<ConsultationDTO> getQueue(Pageable pageable);

    ConsultationDTO assignToAdvisor(Long consultationId, Long advisorId);

    Page<ConsultationDTO> getAdvisorAssigned(Long advisorId, Pageable pageable);

    ConsultationDTO completeConsultation(Long consultationId, Long advisorId, String finalAdvice);

    ConsultationDTO getDetail(Long consultationId, Long requesterId, boolean isAdvisor);
}
