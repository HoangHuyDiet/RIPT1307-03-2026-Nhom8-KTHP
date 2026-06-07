package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.*;
import com.smartfinance.smart_finance_hub.dto.response.*;
import com.smartfinance.smart_finance_hub.dto.response.ConsultationDTO;
import com.smartfinance.smart_finance_hub.dto.response.RagStatusDTO;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.ConsultationService;
import com.smartfinance.smart_finance_hub.service.KnowledgeIngestionService;
import com.smartfinance.smart_finance_hub.service.SupportAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SupportController {

    private final ConsultationService consultationService;
    private final SupportAdminService supportAdminService;
    private final Optional<KnowledgeIngestionService> ingestionService;

    @org.springframework.beans.factory.annotation.Autowired
    public SupportController(
            ConsultationService consultationService,
            SupportAdminService supportAdminService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) KnowledgeIngestionService ingestionService) {
        this.consultationService = consultationService;
        this.supportAdminService = supportAdminService;
        this.ingestionService = Optional.ofNullable(ingestionService);
    }
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

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('SUPPORT_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<SupportUserResponse> data = supportAdminService.getAllUsers();
        return buildResponse(data, "Lấy danh sách người dùng thành công");
    }

    @PostMapping("/users/toggle-status")
    @PreAuthorize("hasRole('SUPPORT_ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@Valid @RequestBody ToggleUserStatusRequest request, Principal principal) {
        SupportUserResponse data = supportAdminService.toggleUserStatus(request, principal.getName());
        return buildResponse(data, "Cập nhật trạng thái thành công");
    }

    @PostMapping("/users/lock-request")
    @PreAuthorize("hasRole('SUPPORT_ADMIN')")
    public ResponseEntity<Map<String, Object>> createLockRequest(@Valid @RequestBody LockRequestCreateDTO request, Principal principal) {
        supportAdminService.createLockRequest(request, principal.getName());
        return buildResponse(null, "Gửi yêu cầu khóa thành công");
    }

    @GetMapping("/lock-requests")
    @PreAuthorize("hasAnyRole('SUPPORT_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllLockRequests() {
        List<LockRequestResponse> data = supportAdminService.getAllLockRequests();
        return buildResponse(data, "Lấy danh sách yêu cầu khóa thành công");
    }

    @PostMapping("/lock-requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveLockRequest(@PathVariable Long id, Principal principal) {
        supportAdminService.approveLockRequest(id, principal.getName());
        return buildResponse(null, "Đã duyệt yêu cầu khóa");
    }

    @PostMapping("/lock-requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectLockRequest(@PathVariable Long id, Principal principal) {
        supportAdminService.rejectLockRequest(id, principal.getName());
        return buildResponse(null, "Đã từ chối yêu cầu khóa");
    }

    @DeleteMapping("/lock-requests/{id}")
    @PreAuthorize("hasRole('SUPPORT_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteLockRequest(@PathVariable Long id) {
        supportAdminService.deleteLockRequest(id);
        return buildResponse(null, "Đã xóa yêu cầu khóa");
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAnyRole('SUPPORT_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAuditLogs(@RequestParam(required = false) String email) {
        List<AuditLogResponse> data = supportAdminService.getAuditLogsByEmail(email);
        return buildResponse(data, "Lấy lịch sử hoạt động thành công");
    }

    @GetMapping("/chat-requests")
    @PreAuthorize("hasAnyRole('SUPPORT_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getChatRequests(@RequestParam(required = false) String type) {
        List<ChatRequestResponse> data = supportAdminService.getAllChatRequests();
        return buildResponse(data, "Lấy danh sách yêu cầu hỗ trợ thành công");
    }

    @PostMapping("/chat-requests/send")
    @PreAuthorize("hasAnyRole('SUPPORT_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> sendChatMessage(@Valid @RequestBody SendChatMessageRequest request, Principal principal) {
        ChatMessageResponse data = supportAdminService.sendChatMessage(request, principal.getName());
        return buildResponse(data, "Gửi tin nhắn thành công");
    }

    @PostMapping("/chat-requests/resolve")
    @PreAuthorize("hasAnyRole('SUPPORT_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> resolveChat(@Valid @RequestBody ResolveChatRequest request) {
        supportAdminService.resolveChat(request);
        return buildResponse(null, "Đã đóng yêu cầu hỗ trợ");
    }

    @GetMapping("/broadcasts")
    @PreAuthorize("hasAnyRole('SUPPORT_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getBroadcasts() {
        List<BroadcastResponse> data = supportAdminService.getAllBroadcasts();
        return buildResponse(data, "Lấy danh sách thông báo thành công");
    }

    @PostMapping("/broadcasts/create")
    @PreAuthorize("hasRole('SUPPORT_ADMIN')")
    public ResponseEntity<Map<String, Object>> createBroadcast(@Valid @RequestBody CreateBroadcastRequest request, Principal principal) {
        BroadcastResponse data = supportAdminService.createBroadcast(request, principal.getName());
        return buildResponse(data, "Tạo thông báo thành công");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return ResponseEntity.ok(response);
    }
}
