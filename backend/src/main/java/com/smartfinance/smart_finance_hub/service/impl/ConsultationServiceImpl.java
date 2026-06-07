package com.smartfinance.smart_finance_hub.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfinance.smart_finance_hub.dto.request.ConsultationCreateRequest;
import com.smartfinance.smart_finance_hub.dto.response.ConsultationDTO;
import com.smartfinance.smart_finance_hub.dto.response.FinancialSnapshotDTO;
import com.smartfinance.smart_finance_hub.entity.ConsultationRequest;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.ConsultationStatus;
import com.smartfinance.smart_finance_hub.repository.ConsultationRequestRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.ConsultationService;
import com.smartfinance.smart_finance_hub.service.FinancialSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRequestRepository consultationRepository;
    private final UserRepository userRepository;
    private final FinancialSnapshotService snapshotService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ConsultationDTO createRequest(Long userId, ConsultationCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User không tồn tại: " + userId));

        String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        FinancialSnapshotDTO snapshot = snapshotService.buildSnapshot(
            userId, currentMonth, request.getConsentScopes());

        String snapshotJson;
        try {
            snapshotJson = objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.error("Lỗi serialize snapshot", e);
            snapshotJson = "{}";
        }

        String consentScopeJson;
        try {
            consentScopeJson = objectMapper.writeValueAsString(request.getConsentScopes());
        } catch (JsonProcessingException e) {
            consentScopeJson = "[]";
        }

        ConsultationRequest consultation = ConsultationRequest.builder()
            .user(user)
            .userQuestion(request.getQuestion())
            .consentScope(consentScopeJson)
            .financialSnapshotJson(snapshotJson)
            .status(ConsultationStatus.NEW)
            .build();

        consultation = consultationRepository.save(consultation);
        log.info("Tạo yêu cầu tư vấn #{} cho user {}", consultation.getId(), userId);

        return toDTO(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultationDTO> getUserRequests(Long userId, Pageable pageable) {
        return consultationRepository.findByUserId(userId, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultationDTO> getQueue(Pageable pageable) {
        return consultationRepository.findByStatus(ConsultationStatus.NEW, pageable).map(this::toDTO);
    }

    @Override
    @Transactional
    public ConsultationDTO assignToAdvisor(Long consultationId, Long advisorId) {
        ConsultationRequest consultation = consultationRepository.findById(consultationId)
            .orElseThrow(() -> new RuntimeException("Yêu cầu tư vấn không tồn tại: " + consultationId));

        if (consultation.getStatus() != ConsultationStatus.NEW) {
            throw new IllegalStateException("Yêu cầu đã được nhận hoặc hoàn thành: " + consultation.getStatus());
        }

        User advisor = userRepository.findById(advisorId)
            .orElseThrow(() -> new RuntimeException("Advisor không tồn tại: " + advisorId));

        consultation.setAdvisor(advisor);
        consultation.setStatus(ConsultationStatus.ASSIGNED);
        consultation.setAssignedAt(LocalDateTime.now());

        consultation = consultationRepository.save(consultation);
        log.info("Chuyên viên {} đã nhận yêu cầu tư vấn #{}", advisorId, consultationId);

        return toDTO(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultationDTO> getAdvisorAssigned(Long advisorId, Pageable pageable) {
        return consultationRepository.findByAdvisorId(advisorId, pageable).map(this::toDTO);
    }

    @Override
    @Transactional
    public ConsultationDTO completeConsultation(Long consultationId, Long advisorId, String finalAdvice) {
        ConsultationRequest consultation = consultationRepository.findById(consultationId)
            .orElseThrow(() -> new RuntimeException("Yêu cầu tư vấn không tồn tại: " + consultationId));

        if (consultation.getAdvisor() == null || !consultation.getAdvisor().getId().equals(advisorId)) {
            throw new SecurityException("Bạn không có quyền hoàn thành yêu cầu tư vấn này");
        }

        consultation.setFinalAdvice(finalAdvice);
        consultation.setStatus(ConsultationStatus.COMPLETED);
        consultation.setCompletedAt(LocalDateTime.now());

        consultation = consultationRepository.save(consultation);
        log.info("Yêu cầu tư vấn #{} đã hoàn thành bởi chuyên viên {}", consultationId, advisorId);

        return toDTO(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationDTO getDetail(Long consultationId, Long requesterId, boolean isAdvisor) {
        ConsultationRequest consultation = consultationRepository.findById(consultationId)
            .orElseThrow(() -> new RuntimeException("Yêu cầu tư vấn không tồn tại: " + consultationId));

        if (!isAdvisor && !consultation.getUser().getId().equals(requesterId)) {
            throw new SecurityException("Bạn không có quyền xem yêu cầu tư vấn này");
        }

        return toDTO(consultation);
    }

    private ConsultationDTO toDTO(ConsultationRequest entity) {
        return ConsultationDTO.builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .userQuestion(entity.getUserQuestion())
            .consentScope(entity.getConsentScope())
            .status(entity.getStatus())
            .aiDraftSummary(entity.getAiDraftJson())
            .finalAdvice(entity.getFinalAdvice())
            .advisorId(entity.getAdvisor() != null ? entity.getAdvisor().getId() : null)
            .createdAt(entity.getCreatedAt())
            .assignedAt(entity.getAssignedAt())
            .completedAt(entity.getCompletedAt())
            .build();
    }
}
