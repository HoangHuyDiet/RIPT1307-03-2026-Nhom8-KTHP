package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.entity.OtpToken;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.UserStatus;
import com.smartfinance.smart_finance_hub.repository.OtpTokenRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
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

    @Value("${app.2fa.otp-length}")
    private int otpLength;

    @Value("${app.2fa.otp-expiry-seconds}")
    private int otpExpirySeconds;

    @Transactional
    public void sendOtp(String email) throws MessagingException {
        log.info("sendOtp param: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        String otpCode = generateAndStoreOtp(email);
        int expiryMinutes = otpExpirySeconds / 60;
        mailService.sendOtpEmail(email, user.getDisplayName(), otpCode, expiryMinutes);

        log.info("sendOtp success: {}", email);
    }

    @Transactional
    public boolean verifyOtpAndActivate(String email, String otpCode) {
        log.info("verifyOtpAndActivate param: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        OtpToken otpToken = otpTokenRepository.findTopByEmailAndIsUsedFalseOrderByIdDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Mã OTP không tồn tại. Vui lòng yêu cầu gửi lại mã mới"));

        if (LocalDateTime.now().isAfter(otpToken.getExpirationTime())) {
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
            throw new IllegalArgumentException("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới");
        }

        boolean isValid = otpToken.getOtpCode().equals(otpCode);

        if (isValid) {
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);

            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);

            log.info("verifyOtpAndActivate success: {} -> ACTIVE", email);
        } else {
            log.warn("verifyOtpAndActivate failed: {}", email);
        }

        return isValid;
    }

    @Transactional
    public void resendOtp(String email) throws MessagingException {
        log.info("resendOtp param: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        String otpCode = generateAndStoreOtp(email);
        int expiryMinutes = otpExpirySeconds / 60;
        mailService.sendOtpEmail(email, user.getDisplayName(), otpCode, expiryMinutes);

        log.info("resendOtp success: {}", email);
    }

    /**
     * Chỉ xác thực OTP mà KHÔNG kích hoạt tài khoản.
     * Dùng cho flow quên mật khẩu.
     */
    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        log.info("verifyOtp param: email={}", email);

        userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        OtpToken otpToken = otpTokenRepository.findTopByEmailAndIsUsedFalseOrderByIdDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Mã OTP không tồn tại. Vui lòng yêu cầu gửi lại mã mới"));

        if (LocalDateTime.now().isAfter(otpToken.getExpirationTime())) {
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
            throw new IllegalArgumentException("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới");
        }

        boolean isValid = otpToken.getOtpCode().equals(otpCode);
        if (isValid) {
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
            log.info("verifyOtp success: {}", email);
        } else {
            log.warn("verifyOtp failed (wrong code): {}", email);
        }
        return isValid;
    }

    public boolean isAccountActive(String email) {
        return userRepository.findByEmail(email)
                .map(user -> UserStatus.ACTIVE.equals(user.getStatus()))
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
