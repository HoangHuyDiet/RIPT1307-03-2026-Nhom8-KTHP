package com.smartfinance.smart_finance_hub.service;

import jakarta.mail.MessagingException;

public interface MailService {

    void sendOtpEmail(String toEmail, String userName, String otpCode, int expiryMinutes)
            throws MessagingException;

    void sendWelcomeEmail(String toEmail, String userName) throws MessagingException;

    void sendResetPasswordEmail(String toEmail, String userName, String resetCode, int expiryMinutes)
            throws MessagingException;
}
