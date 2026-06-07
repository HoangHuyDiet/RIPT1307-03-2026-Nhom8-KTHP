package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.CreateSupportChatRequest;
import com.smartfinance.smart_finance_hub.dto.request.SendChatMessageRequest;
import com.smartfinance.smart_finance_hub.dto.response.ChatMessageResponse;
import com.smartfinance.smart_finance_hub.dto.response.ChatRequestResponse;
import com.smartfinance.smart_finance_hub.service.UserSupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/support")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserSupportController {

    private final UserSupportService userSupportService;

    @GetMapping("/chat-session")
    @PreAuthorize("hasAnyRole('PRO', 'VIP', 'USER')")
    public ResponseEntity<Map<String, Object>> getUserChatSession(Principal principal) {
        List<ChatRequestResponse> data = userSupportService.getUserChatRequests(principal.getName());
        return buildResponse(data, "Lấy danh sách yêu cầu hỗ trợ thành công");
    }

    @PostMapping("/chat-session/create")
    @PreAuthorize("hasAnyRole('PRO', 'VIP', 'USER')")
    public ResponseEntity<Map<String, Object>> createChatSession(@Valid @RequestBody CreateSupportChatRequest request, Principal principal) {
        ChatRequestResponse data = userSupportService.createChatRequest(request, principal.getName());
        return buildResponse(data, "Tạo phiên hỗ trợ thành công");
    }

    @PostMapping("/chat-session/send")
    @PreAuthorize("hasAnyRole('PRO', 'VIP', 'USER')")
    public ResponseEntity<Map<String, Object>> sendChatMessage(@Valid @RequestBody SendChatMessageRequest request, Principal principal) {
        ChatMessageResponse data = userSupportService.sendChatMessage(request, principal.getName());
        return buildResponse(data, "Gửi tin nhắn thành công");
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
