package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.entity.OtpToken;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.repository.OtpTokenRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class TwoFactorAuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final MailService mailService;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    @Value("${app.2fa.otp-length}")
    private int otpLength;

    @Value("${app.2fa.otp-expiry-seconds}")
    private int otpExpirySeconds;

    @Transactional
    public String enableTwoFactor(String email) throws MessagingException {
        log.info("enableTwoFactor param: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        if (user.getTwoFaSecret() != null && !user.getTwoFaSecret().isEmpty()) {
            throw new IllegalStateException("Xác thực 2 lớp đã được bật cho tài khoản này");
        }

        GoogleAuthenticatorKey secretKey = googleAuthenticator.createCredentials();
        String secret = secretKey.getKey();

        user.setTwoFaSecret(secret);
        userRepository.save(user);

        String otpCode = generateAndStoreOtp(email);
        int expiryMinutes = otpExpirySeconds / 60;
        mailService.sendOtpEmail(email, user.getDisplayName(), otpCode, expiryMinutes);

        log.info("enableTwoFactor success: {}", email);
        return secret;
    }

    @Transactional
    public void disableTwoFactor(String email) {
        log.info("disableTwoFactor param: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        if (user.getTwoFaSecret() == null || user.getTwoFaSecret().isEmpty()) {
            throw new IllegalStateException("Xác thực 2 lớp chưa được bật cho tài khoản này");
        }

        user.setTwoFaSecret(null);
        userRepository.save(user);
        otpTokenRepository.deleteByEmail(email);

        log.info("disableTwoFactor success: {}", email);
    }

    public void sendOtp(String email) throws MessagingException {
        log.info("sendOtp param: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        if (user.getTwoFaSecret() == null || user.getTwoFaSecret().isEmpty()) {
            throw new IllegalStateException("Xác thực 2 lớp chưa được bật. Vui lòng bật 2FA trước");
        }

        String otpCode = generateAndStoreOtp(email);
        int expiryMinutes = otpExpirySeconds / 60;
        mailService.sendOtpEmail(email, user.getDisplayName(), otpCode, expiryMinutes);

        log.info("sendOtp success: {}", email);
    }

    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        log.info("verifyOtp param: email={}", email);

        userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        OtpToken otpToken = otpTokenRepository.findTopByEmailAndIsUsedFalseOrderByIdDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Mã OTP không tồn tại. Vui lòng yêu cầu gửi lại mã mới"));

        if (otpToken.isExpired()) {
            otpToken.setIsUsed(true);
            otpTokenRepository.save(otpToken);
            throw new IllegalArgumentException("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới");
        }

        boolean isValid = otpToken.getOtpCode().equals(otpCode);

        if (isValid) {
            otpToken.setIsUsed(true);
            otpTokenRepository.save(otpToken);
            log.info("verifyOtp success: {}", email);
        } else {
            log.warn("verifyOtp failed: {}", email);
        }

        return isValid;
    }

    public boolean isTwoFactorEnabled(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getTwoFaSecret() != null && !user.getTwoFaSecret().isEmpty())
                .orElse(false);
    }

    @Transactional
    private String generateAndStoreOtp(String email) {
        String otpCode = generateRandomOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(otpExpirySeconds);

        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpCode(otpCode)
                .expirationTime(expiryTime)
                .isUsed(false)
                .build();

        otpTokenRepository.save(otpToken);
        log.debug("generateAndStoreOtp: email={}, expiryTime={}", email, expiryTime);
        return otpCode;
    }

    private String generateRandomOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
