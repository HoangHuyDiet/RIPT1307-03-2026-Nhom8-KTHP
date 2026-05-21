package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.OtpSendRequest;
import com.smartfinance.smart_finance_hub.dto.request.OtpVerifyRequest;
import com.smartfinance.smart_finance_hub.dto.request.TwoFactorEnableRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.service.TwoFactorAuthService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/enable")
    public ResponseEntity<ApiResponse<Map<String, String>>> enableTwoFactor(
            @Valid @RequestBody TwoFactorEnableRequest request) throws MessagingException {
        log.info("enableTwoFactor param: {}", request);

        String secretKey = twoFactorAuthService.enableTwoFactor(request.getEmail());
        Map<String, String> data = Map.of("secretKey", secretKey);

        return ResponseEntity.ok(
                ApiResponse.success("Đã bật xác thực 2 lớp. Mã OTP đã được gửi tới email của bạn", data)
        );
    }

    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<Void>> disableTwoFactor(
            @Valid @RequestBody TwoFactorEnableRequest request) {
        log.info("disableTwoFactor param: {}", request);

        twoFactorAuthService.disableTwoFactor(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("Đã tắt xác thực 2 lớp cho tài khoản")
        );
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody OtpSendRequest request) throws MessagingException {
        log.info("sendOtp param: {}", request);

        twoFactorAuthService.sendOtp(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("Mã OTP đã được gửi tới email " + request.getEmail())
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        log.info("verifyOtp param: email={}", request.getEmail());

        boolean isValid = twoFactorAuthService.verifyOtp(request.getEmail(), request.getOtpCode());

        if (isValid) {
            return ResponseEntity.ok(
                    ApiResponse.success("Xác thực OTP thành công")
            );
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Mã OTP không chính xác. Vui lòng thử lại"));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkStatus(
            @RequestParam String email) {
        log.info("checkStatus param: email={}", email);

        boolean isEnabled = twoFactorAuthService.isTwoFactorEnabled(email);
        Map<String, Boolean> data = Map.of("enabled", isEnabled);

        return ResponseEntity.ok(
                ApiResponse.success(
                        isEnabled ? "Xác thực 2 lớp đang BẬT" : "Xác thực 2 lớp đang TẮT",
                        data
                )
        );
    }
}
