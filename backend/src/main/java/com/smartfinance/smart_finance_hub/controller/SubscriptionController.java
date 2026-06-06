package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.SubscriptionCheckoutRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.dto.response.SubscriptionCheckoutResponse;
import com.smartfinance.smart_finance_hub.dto.response.SubscriptionPlanResponse;
import com.smartfinance.smart_finance_hub.dto.response.UserSubscriptionResponse;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.SubscriptionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.success(
            "Lay danh sach goi Pro thanh cong",
            subscriptionService.getActivePlans()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> getMine() {
        return ResponseEntity.ok(ApiResponse.success(
            "Lay trang thai goi Pro thanh cong",
            subscriptionService.getCurrentSubscription(getCurrentUserId())
        ));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<SubscriptionCheckoutResponse>> checkout(
            @Valid @RequestBody SubscriptionCheckoutRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            "Tao QR thanh toan PayOS thanh cong",
            subscriptionService.createCheckout(getCurrentUserId(), request)
        ));
    }

    @GetMapping("/orders/{orderCode}")
    public ResponseEntity<ApiResponse<SubscriptionCheckoutResponse>> getOrder(
            @PathVariable Long orderCode) {
        return ResponseEntity.ok(ApiResponse.success(
            "Lay trang thai thanh toan thanh cong",
            subscriptionService.getOrder(getCurrentUserId(), orderCode)
        ));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Khong the xac thuc nguoi dung hien tai!");
    }
}
