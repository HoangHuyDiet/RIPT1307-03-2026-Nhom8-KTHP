package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.entity.OtpToken;
import com.smartfinance.smart_finance_hub.repository.OtpTokenRepository;
import com.smartfinance.smart_finance_hub.service.EmailService;
import com.smartfinance.smart_finance_hub.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

  private final OtpTokenRepository otpTokenRepository;

  private final EmailService emailService;

  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public void generateAndSendOtp(String email) {
    int otpValue = 100000 + secureRandom.nextInt(900000);
    String otpCode = String.valueOf(otpValue);

    OtpToken otpToken = OtpToken.builder()
        .email(email)
        .otpCode(otpCode)
        .expirationTime(LocalDateTime.now().plusMinutes(5))
        .isUsed(false)
        .build();

    otpTokenRepository.save(otpToken);
    log.info("Đã khởi tạo và lưu OTP cho user: {}", email);

    emailService.sendOtpEmail(email, otpCode);
  }
}
