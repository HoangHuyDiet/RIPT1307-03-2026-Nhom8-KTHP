package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.LoginRequest;
import com.smartfinance.smart_finance_hub.dto.LoginResponse;
import com.smartfinance.smart_finance_hub.dto.RegisterRequest;
import com.smartfinance.smart_finance_hub.dto.request.ChangePasswordRequest;
import com.smartfinance.smart_finance_hub.dto.request.ForgotPasswordRequest;
import com.smartfinance.smart_finance_hub.dto.request.GoogleLoginRequest;
import com.smartfinance.smart_finance_hub.dto.request.RequestPasswordChangeRequest;
import java.util.UUID;

import com.smartfinance.smart_finance_hub.entity.Role;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.entity.UserRole;
import com.smartfinance.smart_finance_hub.enums.UserStatus;
import com.smartfinance.smart_finance_hub.exception.business.UserAlreadyExistsException;
import com.smartfinance.smart_finance_hub.repository.RoleRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.repository.UserRoleRepository;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.security.JwtUtils;
import com.smartfinance.smart_finance_hub.service.AuthService;
import com.smartfinance.smart_finance_hub.service.TwoFactorAuthService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("registerUser failed, email is already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email này đã được sử dụng!");
        }

        log.info("registerUser success: {}", request.getEmail());
        User newUser = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .displayName(request.getDisplayName())
            .status(UserStatus.INACTIVE)
            .build();

        userRepository.save(newUser);

        Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new RuntimeException("Role USER không tồn tại trong hệ thống!"));
        UserRole ur = UserRole.builder()
            .user(newUser)
            .role(userRole)
            .build();
        userRoleRepository.save(ur);
        log.info("Đã gán role USER cho user: {}", newUser.getEmail());

        try {
            twoFactorAuthService.sendOtp(request.getEmail());
            log.info("Đã tạo tài khoản và gửi mã OTP cho: {}", newUser.getEmail());
        } catch (MessagingException e) {
            log.error("Lỗi hệ thống khi gửi email OTP đến {}: {}", newUser.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email OTP, vui lòng thử lại sau!");
        }
    }

    @Override
    public void verifyAccount(String email, String otpCode) {
        boolean isValid = twoFactorAuthService.verifyOtpAndActivate(email, otpCode);
        if (!isValid) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã được sử dụng!");
        }
        log.info("Xác thực tài khoản thành công cho user: {}", email);
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        if (UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đã được kích hoạt");
        }

        if (UserStatus.BANNED.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đã bị khóa");
        }

        try {
            twoFactorAuthService.resendOtp(email);
            log.info("Đã gửi lại mã OTP cho: {}", email);
        } catch (MessagingException e) {
            log.error("Lỗi gửi lại email OTP cho {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi lại email OTP, vui lòng thử lại sau!");
        }
    }

    private LoginResponse buildLoginResponse(User user) {
        CustomUserDetails userDetails = CustomUserDetails.build(user);
        String token = jwtUtils.generateToken(userDetails);

        List<String> roleNames = (user.getUserRoles() != null)
            ? user.getUserRoles().stream()
                .filter(ur -> ur.getExpiredAt() == null || ur.getExpiredAt().isAfter(java.time.LocalDateTime.now()))
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList())
            : Collections.emptyList();

        return LoginResponse.builder()
            .token(token)
            .email(user.getEmail())
            .displayName(user.getDisplayName())
            .roles(roleNames)
            .build();
    }

    private User createGoogleUser(String email, String displayName) {
        User newUser = User.builder()
            .email(email)
            .password(passwordEncoder.encode("GOOGLE_AUTH_" + UUID.randomUUID()))
            .displayName(displayName == null || displayName.isBlank() ? email : displayName)
            .status(UserStatus.ACTIVE)
            .build();
        userRepository.save(newUser);

        Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new RuntimeException("Role USER không tồn tại trong hệ thống!"));
        userRoleRepository.save(UserRole.builder()
            .user(newUser)
            .role(userRole)
            .build());
        return newUser;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("login request: {}", request.getEmail());

        User user = userRepository.findByEmailWithRoles(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }

        if (UserStatus.INACTIVE.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản chưa xác thực OTP. Vui lòng kiểm tra email");
        }

        if (UserStatus.BANNED.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đã bị khóa");
        }

        log.info("login success: {}", request.getEmail());
        return buildLoginResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        log.info("Google login request with token: {}", request.getToken());
        Map<String, Object> profile;
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(request.getToken());
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );
            profile = response.getBody();
        } catch (Exception e) {
            log.error("Lỗi khi kết nối với Google API để lấy thông tin user info: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Không thể xác thực tài khoản Google (lỗi kết nối hoặc token không hợp lệ)");
        }

        if (profile == null || profile.get("email") == null) {
            throw new IllegalArgumentException("Không thể xác thực tài khoản Google");
        }

        String email = String.valueOf(profile.get("email")).trim().toLowerCase();
        boolean emailVerified = Boolean.parseBoolean(String.valueOf(profile.getOrDefault("email_verified", "false")));
        if (!emailVerified) {
            throw new IllegalArgumentException("Email Google chưa được xác thực");
        }

        String displayName = String.valueOf(profile.getOrDefault("name", email));
        User user = userRepository.findByEmailWithRoles(email)
            .orElseGet(() -> createGoogleUser(email, displayName));

        if (UserStatus.BANNED.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đã bị khóa");
        }

        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        User userWithRoles = userRepository.findByEmailWithRoles(email).orElse(user);
        log.info("Google login success: {}", email);
        return buildLoginResponse(userWithRoles);
    }

    @Override
    public LoginResponse getMe(String email) {
        log.info("getMe request for email: {}", email);
        User user = userRepository.findByEmailWithRoles(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));
        return buildLoginResponse(user);
    }


    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("forgotPassword request cho email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống!"));

        if (UserStatus.BANNED.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đã bị khóa, không thể đổi mật khẩu");
        }

        try {
            twoFactorAuthService.sendOtp(request.getEmail());
            log.info("Đã gửi mã OTP đặt lại mật khẩu cho: {}", request.getEmail());
        } catch (MessagingException e) {
            log.error("Lỗi gửi email OTP reset password cho {}: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email OTP, vui lòng thử lại sau!");
        }
    }

    @Override
    @Transactional
    public void resetPasswordWithOtp(String email, String otpCode, String newPassword) {
        log.info("resetPasswordWithOtp cho email: {}", email);

        boolean isValid = twoFactorAuthService.verifyOtp(email, otpCode);
        if (!isValid) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn!");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống!"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Đổi mật khẩu thành công cho user: {}", email);
    }

    @Override
    @Transactional
    public void requestPasswordChange(String email, RequestPasswordChangeRequest request) {
        log.info("requestPasswordChange cho email: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"));

        if (UserStatus.BANNED.equals(user.getStatus())) {
            throw new IllegalStateException("Tài khoản đã bị khóa, không thể đổi mật khẩu");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }

        try {
            twoFactorAuthService.sendOtp(email);
            log.info("Đã gửi mã OTP đổi mật khẩu cho: {}", email);
        } catch (MessagingException e) {
            log.error("Lỗi gửi email OTP cho {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email OTP, vui lòng thử lại sau!");
        }
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("changePassword cho email: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }

        boolean isValid = twoFactorAuthService.verifyOtp(email, request.getOtpCode());
        if (!isValid) {
            throw new IllegalArgumentException("Mã OTP không chính xác hoặc đã hết hạn!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Đổi mật khẩu thành công cho user: {}", email);
    }
}

