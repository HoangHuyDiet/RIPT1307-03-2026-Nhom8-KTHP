package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.OtpSendRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.service.TwoFactorAuthService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody OtpSendRequest request) throws MessagingException {
        log.info("sendOtp param: {}", request);

        twoFactorAuthService.sendOtp(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("Mã OTP đã được gửi tới email " + request.getEmail())
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody OtpSendRequest request) throws MessagingException {
        log.info("resendOtp param: {}", request);

        twoFactorAuthService.resendOtp(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("Mã OTP mới đã được gửi lại tới email " + request.getEmail())
        );
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkStatus(
            @RequestParam String email) {
        log.info("checkStatus param: email={}", email);

        boolean isActive = twoFactorAuthService.isAccountActive(email);
        Map<String, Boolean> data = Map.of("active", isActive);

        return ResponseEntity.ok(
                ApiResponse.success(
                        isActive ? "Tài khoản đang ACTIVE" : "Tài khoản đang INACTIVE - cần xác thực OTP",
                        data
                )
        );
    }
}

