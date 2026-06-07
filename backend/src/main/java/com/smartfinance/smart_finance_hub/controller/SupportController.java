package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.response.ConsultationDTO;
import com.smartfinance.smart_finance_hub.dto.response.RagStatusDTO;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.ConsultationService;
import com.smartfinance.smart_finance_hub.service.KnowledgeIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Support Admin controller — chỉ cho ROLE_SUPPORT_ADMIN.
 * Endpoint: /api/support/**
 */
@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final ConsultationService consultationService;
    private final Optional<KnowledgeIngestionService> ingestionService;

    @Autowired
    public SupportController(
            ConsultationService consultationService,
            @Autowired(required = false) KnowledgeIngestionService ingestionService) {
        this.consultationService = consultationService;
        this.ingestionService = Optional.ofNullable(ingestionService);
    }

    // ==================== Consultation Queue ====================

    /** GET /api/support/consultations/queue — Xem hàng chờ tư vấn */
    @GetMapping("/consultations/queue")
    @PreAuthorize("hasAuthority('CONSULTATION_VIEW_QUEUE')")
    public ResponseEntity<Page<ConsultationDTO>> getQueue(Pageable pageable) {
        return ResponseEntity.ok(consultationService.getQueue(pageable));
    }

    /** POST /api/support/consultations/{id}/assign — Nhận yêu cầu tư vấn */
    @PostMapping("/consultations/{id}/assign")
    @PreAuthorize("hasAuthority('CONSULTATION_ASSIGN_SELF')")
    public ResponseEntity<ConsultationDTO> assign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        ConsultationDTO dto = consultationService.assignToAdvisor(id, userDetails.getId());
        return ResponseEntity.ok(dto);
    }

    /** GET /api/support/consultations/assigned — Xem yêu cầu đã nhận */
    @GetMapping("/consultations/assigned")
    @PreAuthorize("hasAuthority('CONSULTATION_VIEW_ASSIGNED')")
    public ResponseEntity<Page<ConsultationDTO>> getAssigned(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {

        return ResponseEntity.ok(
            consultationService.getAdvisorAssigned(userDetails.getId(), pageable));
    }

    /** GET /api/support/consultations/{id} — Xem chi tiết yêu cầu */
    @GetMapping("/consultations/{id}")
    @PreAuthorize("hasAuthority('CONSULTATION_VIEW_ASSIGNED')")
    public ResponseEntity<ConsultationDTO> getDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        ConsultationDTO dto = consultationService.getDetail(id, userDetails.getId(), true);
        return ResponseEntity.ok(dto);
    }

    /** POST /api/support/consultations/{id}/complete — Hoàn thành tư vấn */
    @PostMapping("/consultations/{id}/complete")
    @PreAuthorize("hasAuthority('CONSULTATION_COMPLETE')")
    public ResponseEntity<ConsultationDTO> complete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String finalAdvice = body.get("finalAdvice");
        if (finalAdvice == null || finalAdvice.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ConsultationDTO dto = consultationService.completeConsultation(
            id, userDetails.getId(), finalAdvice);
        return ResponseEntity.ok(dto);
    }

    // ==================== RAG Management ====================

    /** POST /api/support/rag/rebuild — Kích hoạt rebuild Vector Store */
    @PostMapping("/rag/rebuild")
    @PreAuthorize("hasAuthority('RAG_REBUILD')")
    public ResponseEntity<?> triggerRebuild() {
        if (ingestionService.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "RAG chưa được kích hoạt (ai.rag.enabled=false)"
            ));
        }

        boolean accepted = ingestionService.get().requestRebuild();
        if (accepted) {
            return ResponseEntity.accepted().body(Map.of(
                "success", true,
                "message", "Đã bắt đầu rebuild Vector Store"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "message", "Rebuild đang chạy — vui lòng đợi"
            ));
        }
    }

    /** GET /api/support/rag/status — Xem trạng thái RAG */
    @GetMapping("/rag/status")
    @PreAuthorize("hasAuthority('RAG_STATUS_VIEW')")
    public ResponseEntity<?> getRagStatus() {
        if (ingestionService.isEmpty()) {
            return ResponseEntity.ok(RagStatusDTO.builder()
                .ragEnabled(false)
                .message("RAG chưa được kích hoạt")
                .build());
        }

        return ResponseEntity.ok(ingestionService.get().getStatus());
    }
}
