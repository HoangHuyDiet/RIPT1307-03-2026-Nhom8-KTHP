package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.service.SubscriptionService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/payos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PayosWebhookController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.info("PayOS webhook received: code={}, success={}", payload.get("code"), payload.get("success"));
        subscriptionService.handlePayosWebhook(payload);
        return ResponseEntity.ok(ApiResponse.success("Webhook PayOS processed"));
    }
}
