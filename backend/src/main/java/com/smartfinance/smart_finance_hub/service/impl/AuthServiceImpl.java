package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.LoginRequest;
import com.smartfinance.smart_finance_hub.dto.LoginResponse;
import com.smartfinance.smart_finance_hub.dto.RegisterRequest;
import com.smartfinance.smart_finance_hub.dto.request.ForgotPasswordRequest;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.UserStatus;
import com.smartfinance.smart_finance_hub.exception.business.UserAlreadyExistsException;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
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

    @Override
    public LoginResponse login(LoginRequest request) {
      log.info("login request: {}", request.getEmail());

      User user = userRepository.findByEmail(request.getEmail())
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

      CustomUserDetails userDetails = CustomUserDetails.build(user);
      String token = jwtUtils.generateToken(userDetails);

      log.info("login success: {}", request.getEmail());

      return LoginResponse.builder()
          .token(token)
          .email(user.getEmail())
          .displayName(user.getDisplayName())
          .build();
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
}
