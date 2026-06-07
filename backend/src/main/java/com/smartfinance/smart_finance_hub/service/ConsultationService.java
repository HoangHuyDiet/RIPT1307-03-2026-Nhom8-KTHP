package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.ConsultationCreateRequest;
import com.smartfinance.smart_finance_hub.dto.response.ConsultationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConsultationService {

    /** Người dùng tạo yêu cầu tư vấn */
    ConsultationDTO createRequest(Long userId, ConsultationCreateRequest request);

    /** Người dùng xem danh sách yêu cầu của mình */
    Page<ConsultationDTO> getUserRequests(Long userId, Pageable pageable);

    /** Chuyên viên xem hàng chờ (status = NEW) */
    Page<ConsultationDTO> getQueue(Pageable pageable);

    /** Chuyên viên nhận yêu cầu tư vấn */
    ConsultationDTO assignToAdvisor(Long consultationId, Long advisorId);

    /** Chuyên viên xem yêu cầu đã nhận */
    Page<ConsultationDTO> getAdvisorAssigned(Long advisorId, Pageable pageable);

    /** Chuyên viên gửi lời khuyên cuối cùng và hoàn thành */
    ConsultationDTO completeConsultation(Long consultationId, Long advisorId, String finalAdvice);

    /** Lấy chi tiết một yêu cầu tư vấn */
    ConsultationDTO getDetail(Long consultationId, Long requesterId, boolean isAdvisor);
}
