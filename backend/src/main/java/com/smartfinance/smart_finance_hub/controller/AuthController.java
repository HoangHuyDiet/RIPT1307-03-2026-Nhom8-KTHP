package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.LoginRequest;
import com.smartfinance.smart_finance_hub.dto.LoginResponse;
import com.smartfinance.smart_finance_hub.dto.RegisterRequest;
import com.smartfinance.smart_finance_hub.dto.ResendOtpRequest;
import com.smartfinance.smart_finance_hub.dto.VerifyRequest;
import com.smartfinance.smart_finance_hub.dto.request.ChangePasswordRequest;
import com.smartfinance.smart_finance_hub.dto.request.ForgotPasswordRequest;
import com.smartfinance.smart_finance_hub.dto.request.GoogleLoginRequest;
import com.smartfinance.smart_finance_hub.dto.request.RequestPasswordChangeRequest;
import com.smartfinance.smart_finance_hub.dto.request.RefreshTokenRequest;
import com.smartfinance.smart_finance_hub.dto.request.ResetPasswordOtpRequest;
import com.smartfinance.smart_finance_hub.service.AuthService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
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

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
      log.info("resend-otp request for email: {}", request.getEmail());

      authService.resendOtp(request.getEmail());

      Map<String, Object> response = new HashMap<>();
      response.put("status", 200);
      response.put("message", "Mã OTP mới đã được gửi lại");

      return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
      log.info("login request: {}", request.getEmail());

      LoginResponse loginResponse = authService.login(request);

      Map<String, Object> response = new HashMap<>();
      response.put("status", 200);
      response.put("message", "Đăng nhập thành công");
      response.put("data", loginResponse);

      return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        log.info("Google login request");
        LoginResponse loginResponse = authService.loginWithGoogle(request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Đăng nhập bằng Google thành công");
        response.put("data", loginResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse loginResponse = authService.refreshToken(request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Làm mới phiên đăng nhập thành công");
        response.put("data", loginResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
      log.info("forgot-password request for email: {}", request.getEmail());

      authService.forgotPassword(request);

      Map<String, Object> response = new HashMap<>();
      response.put("status", 200);
      response.put("message", "Mã OTP đã được gửi đến email của bạn!");

      return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordOtpRequest request) {
      log.info("reset-password request for email: {}", request.getEmail());

      authService.resetPasswordWithOtp(request.getEmail(), request.getOtpCode(), request.getNewPassword());

      Map<String, Object> response = new HashMap<>();
      response.put("status", 200);
      response.put("message", "Đổi mật khẩu thành công! Bạn có thể đăng nhập ngay.");

      return ResponseEntity.ok(response);
    }

    @PostMapping("/request-password-change")
    public ResponseEntity<Map<String, Object>> requestPasswordChange(
            @Valid @RequestBody RequestPasswordChangeRequest request) {
        String email = getAuthenticatedEmail();
        log.info("request-password-change for email: {}", email);

        authService.requestPasswordChange(email, request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Mã OTP đã được gửi đến email của bạn!");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        String email = getAuthenticatedEmail();
        log.info("change-password for email: {}", email);

        authService.changePassword(email, request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe() {
        String email = getAuthenticatedEmail();
        log.info("getMe request for email: {}", email);

        com.smartfinance.smart_finance_hub.dto.LoginResponse loginResponse = authService.getMe(email);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Lấy thông tin người dùng thành công");
        response.put("data", loginResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy email của user đang đăng nhập từ SecurityContext (JWT token).
     */
    private String getAuthenticatedEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        throw new IllegalStateException("Bạn cần đăng nhập để thực hiện chức năng này!");
    }
}

