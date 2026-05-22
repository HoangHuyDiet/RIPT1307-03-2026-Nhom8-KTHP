package com.smartfinance.smart_finance_hub.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    public void sendOtpEmail(String toEmail, String userName, String otpCode, int expiryMinutes)
            throws MessagingException {
        log.info("sendOtpEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("otpCode", otpCode);
        context.setVariable("expiryMinutes", expiryMinutes);
        context.setVariable("appName", fromName);

        String htmlContent = templateEngine.process("email/otp-email", context);
        sendHtmlEmail(toEmail, "🔐 Mã xác thực OTP - " + fromName, htmlContent);

        log.info("sendOtpEmail success: toEmail={}", toEmail);
    }

    public void sendWelcomeEmail(String toEmail, String userName) throws MessagingException {
        log.info("sendWelcomeEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("appName", fromName);
        context.setVariable("loginUrl", "http://localhost:8000");

        String htmlContent = templateEngine.process("email/welcome-email", context);
        sendHtmlEmail(toEmail, "🎉 Chào mừng bạn đến với " + fromName + "!", htmlContent);

        log.info("sendWelcomeEmail success: toEmail={}", toEmail);
    }

    public void sendResetPasswordEmail(String toEmail, String userName, String resetCode, int expiryMinutes)
            throws MessagingException {
        log.info("sendResetPasswordEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("resetCode", resetCode);
        context.setVariable("expiryMinutes", expiryMinutes);
        context.setVariable("appName", fromName);

        String htmlContent = templateEngine.process("email/reset-password-email", context);
        sendHtmlEmail(toEmail, "🔑 Yêu cầu đặt lại mật khẩu - " + fromName, htmlContent);

        log.info("sendResetPasswordEmail success: toEmail={}", toEmail);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(fromEmail, fromName);
        } catch (UnsupportedEncodingException e) {
            log.warn("setFrom fallback: {}", e.getMessage());
            helper.setFrom(fromEmail);
        }

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
