package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.*;
import com.smartfinance.smart_finance_hub.entity.OtpToken;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.UserStatus;
import com.smartfinance.smart_finance_hub.exception.business.UserAlreadyExistsException;
import com.smartfinance.smart_finance_hub.repository.OtpTokenRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.security.JwtUtils;
import com.smartfinance.smart_finance_hub.service.AuthService;
import com.smartfinance.smart_finance_hub.service.OtpService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpTokenRepository otpTokenRepository;
    private final OtpService otpService;
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

        otpService.generateAndSendOtp(request.getEmail());
        log.info("Đã tạo tài khoản và gửi mã OTP cho: {}", newUser.getEmail());
    }

    @Override
    public void verifyAccount(String email, String otpCode) {
      OtpToken otpToken = otpTokenRepository.findByEmailAndOtpCodeAndIsUsedFalse(email, otpCode)
          .orElseThrow(() -> new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã được sử dụng!"));

      if (otpToken.getExpirationTime().isBefore(LocalDateTime.now())) {
        throw new IllegalArgumentException("Mã OTP đã hết hạn, vui lòng yêu cầu gửi lại mã mới!");
      }

      otpToken.setUsed(true);
      otpTokenRepository.save(otpToken);

      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"));

      user.setStatus(UserStatus.ACTIVE);
      userRepository.save(user);

      log.info("Xác thực tài khoản thành công cho user: {}", email);
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
}