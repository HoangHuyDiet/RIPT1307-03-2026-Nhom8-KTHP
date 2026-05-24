package com.smartfinance.smart_finance_hub.service;

import jakarta.mail.MessagingException;

public interface TwoFactorAuthService {

    void sendOtp(String email) throws MessagingException;

    boolean verifyOtpAndActivate(String email, String otpCode);

    void resendOtp(String email) throws MessagingException;

    boolean verifyOtp(String email, String otpCode);

    boolean isAccountActive(String email);
}
