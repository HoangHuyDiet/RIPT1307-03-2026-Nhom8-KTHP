package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.RegisterRequest;
import com.smartfinance.smart_finance_hub.dto.VerifyRequest;
import com.smartfinance.smart_finance_hub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("register request: {}", request);
        authService.register(request);
        log.info("register success: {}", request.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "Đăng ký tài khoản thành công!");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify-account")
    public ResponseEntity<?> verifyAccount(@Valid @RequestBody VerifyRequest request) {
      authService.verifyAccount(request.getEmail(), request.getOtpCode());

      return ResponseEntity.ok(Map.of(
          "status", 200,
          "message", "Xác thực tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ."
      ));
    }
}