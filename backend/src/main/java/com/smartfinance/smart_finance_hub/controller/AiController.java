package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.AiChatRequest;
import com.smartfinance.smart_finance_hub.dto.response.AiAvailabilityDTO;
import com.smartfinance.smart_finance_hub.dto.response.AiChatResponse;
import com.smartfinance.smart_finance_hub.dto.response.AiInsightDTO;
import com.smartfinance.smart_finance_hub.enums.AiErrorCode;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.AiAccessService;
import com.smartfinance.smart_finance_hub.service.AiAvailabilityService;
import com.smartfinance.smart_finance_hub.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiAvailabilityService availabilityService;
    private final AiAccessService accessService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AiChatRequest request) {
        if (!accessService.canUseAi(userDetails)) {
            return ResponseEntity.ok(AiChatResponse.builder()
                .sessionId(request.getSessionId())
                .reply(accessService.getUpgradeMessage())
                .aiEnabled(false)
                .ragAvailable(false)
                .errorCode(AiErrorCode.AI_PRO_REQUIRED.name())
                .citations(Collections.emptyList())
                .build());
        }
        return ResponseEntity.ok(aiService.chat(userDetails.getId(), request));
    }

    @GetMapping("/insight")
    public ResponseEntity<AiInsightDTO> getInsight(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "false") boolean forceRefresh) {
        String targetMonth = month;
        if (targetMonth == null || targetMonth.isBlank()) {
            targetMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        if (!accessService.canUseAi(userDetails)) {
            return ResponseEntity.ok(AiInsightDTO.builder()
                .month(targetMonth)
                .summary(accessService.getUpgradeMessage())
                .fromCache(false)
                .errorCode(AiErrorCode.AI_PRO_REQUIRED.name())
                .build());
        }
        return ResponseEntity.ok(aiService.getMonthlyInsight(userDetails.getId(), targetMonth, forceRefresh));
    }

    @GetMapping("/status")
    public ResponseEntity<AiAvailabilityDTO> getStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        AiAvailabilityDTO availability = availabilityService.getAvailability();
        availability.setProRequired(accessService.isProRequired());
        availability.setAiAccessible(accessService.canUseAi(userDetails));
        availability.setAllowedRoles(accessService.getAllowedRoles());
        if (accessService.isProRequired() && !accessService.canUseAi(userDetails)) {
            availability.setAiAvailable(false);
            availability.setErrorCode(AiErrorCode.AI_PRO_REQUIRED);
            availability.setReason(accessService.getUpgradeMessage());
        }
        return ResponseEntity.ok(availability);
    }
}
