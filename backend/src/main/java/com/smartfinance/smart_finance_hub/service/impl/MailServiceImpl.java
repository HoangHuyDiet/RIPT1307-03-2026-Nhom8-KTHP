package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.service.MailService;
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
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from-email:no-reply@smartfinance.com}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendOtpEmail(String toEmail, String userName, String otpCode, int expiryMinutes)
            throws MessagingException {
        log.info("sendOtpEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("otpCode", otpCode);
        context.setVariable("expiryMinutes", expiryMinutes);
        context.setVariable("appName", fromName);

        String htmlContent = templateEngine.process("email/otp-email", context);
        sendHtmlEmail(toEmail, "OTP verification - " + fromName, htmlContent);

        log.info("sendOtpEmail success: toEmail={}", toEmail);
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String userName) throws MessagingException {
        log.info("sendWelcomeEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("appName", fromName);
        context.setVariable("loginUrl", "http://localhost:8000");

        String htmlContent = templateEngine.process("email/welcome-email", context);
        sendHtmlEmail(toEmail, "Welcome to " + fromName, htmlContent);

        log.info("sendWelcomeEmail success: toEmail={}", toEmail);
    }

    @Override
    public void sendResetPasswordEmail(String toEmail, String userName, String resetCode, int expiryMinutes)
            throws MessagingException {
        log.info("sendResetPasswordEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("resetCode", resetCode);
        context.setVariable("expiryMinutes", expiryMinutes);
        context.setVariable("appName", fromName);

        String htmlContent = templateEngine.process("email/reset-password-email", context);
        sendHtmlEmail(toEmail, "Password reset request - " + fromName, htmlContent);

        log.info("sendResetPasswordEmail success: toEmail={}", toEmail);
    }

    @Override
    public void sendFundInvitationEmail(String toEmail, String userName, String invitedByName, String invitedByEmail,
            String fundName, String token, int expiryHours) throws MessagingException {
        log.info("sendFundInvitationEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("invitedByName", invitedByName);
        context.setVariable("invitedByEmail", invitedByEmail);
        context.setVariable("fundName", fundName);
        context.setVariable("expiryHours", expiryHours);
        context.setVariable("appName", fromName);
        context.setVariable("actionUrl", invitationActionUrl(token));

        String htmlContent = templateEngine.process("email/fund-invitation", context);
        sendHtmlEmailWithSender(toEmail, "Lời mời tham gia quỹ - " + fundName, htmlContent, invitedByEmail, invitedByName);
    }

    @Override
    public void sendKickProposalEmail(String toEmail, String userName, String fundName,
            String kickedByName, String reason, String token) throws MessagingException {
        log.info("sendKickProposalEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("kickedByName", kickedByName);
        context.setVariable("fundName", fundName);
        context.setVariable("reason", reason);
        context.setVariable("appName", fromName);
        context.setVariable("actionUrl", invitationActionUrl(token));

        String htmlContent = templateEngine.process("email/kick-proposal", context);
        sendHtmlEmail(toEmail, "Kick proposal - " + fundName, htmlContent);
    }

    @Override
    public void sendDisbandProposalEmail(String toEmail, String userName, String fundName,
            String proposedByName, String proposedByEmail, String reason, String token) throws MessagingException {
        log.info("sendDisbandProposalEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("proposedByName", proposedByName);
        context.setVariable("proposedByEmail", proposedByEmail);
        context.setVariable("fundName", fundName);
        context.setVariable("reason", reason);
        context.setVariable("appName", fromName);
        context.setVariable("actionUrl", invitationActionUrl(token));

        String htmlContent = templateEngine.process("email/disband-proposal", context);
        sendHtmlEmailWithSender(toEmail, "Disband proposal - " + fundName, htmlContent, proposedByEmail, proposedByName);
    }

    @Override
    public void sendDisbandConfirmationEmail(String toEmail, String userName, String fundName)
            throws MessagingException {
        log.info("sendDisbandConfirmationEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("fundName", fundName);
        context.setVariable("appName", fromName);

        String htmlContent = templateEngine.process("email/disband-confirmation", context);
        sendHtmlEmail(toEmail, "Fund disbanded - " + fundName, htmlContent);
    }

    @Override
    public void sendFundNotificationEmail(String toEmail, String userName, String fundName,
            String subject, String content) throws MessagingException {
        log.info("sendFundNotificationEmail param: toEmail={}", toEmail);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("fundName", fundName);
        context.setVariable("notificationContent", content);
        context.setVariable("appName", fromName);

        String htmlContent = templateEngine.process("email/fund-notification", context);
        sendHtmlEmail(toEmail, subject + " - " + fundName, htmlContent);
    }

    private String invitationActionUrl(String token) {
        return frontendUrl + "/funds/verify?token=" + token;
    }

    @Value("${app.mail.gmail-api.enabled:false}")
    private boolean gmailApiEnabled;

    @Value("${app.mail.gmail-api.client-id:}")
    private String gmailClientId;

    @Value("${app.mail.gmail-api.client-secret:}")
    private String gmailClientSecret;

    @Value("${app.mail.gmail-api.refresh-token:}")
    private String gmailRefreshToken;

    @Value("${app.mail.brevo-api-key:}")
    private String brevoApiKey;

    private boolean isGmailApiConfigured() {
        return gmailClientId != null && !gmailClientId.trim().isEmpty() &&
               gmailClientSecret != null && !gmailClientSecret.trim().isEmpty() &&
               gmailRefreshToken != null && !gmailRefreshToken.trim().isEmpty();
    }

    private String getGmailAccessToken() throws Exception {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + gmailClientId.trim()
                + "&client_secret=" + gmailClientSecret.trim()
                + "&refresh_token=" + gmailRefreshToken.trim()
                + "&grant_type=refresh_token";

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(body, headers);
        
        java.util.Map<?, ?> response = restTemplate.postForObject("https://oauth2.googleapis.com/token", request, java.util.Map.class);
        if (response != null && response.containsKey("access_token")) {
            return (String) response.get("access_token");
        }
        throw new RuntimeException("Failed to retrieve access token from Google OAuth2");
    }

    private void sendEmailViaGmailApi(String toEmail, String subject, String htmlContent, String replyToEmail, String replyToName) throws MessagingException {
        try {
            String accessToken = getGmailAccessToken();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            if (replyToEmail != null && !replyToEmail.trim().isEmpty()) {
                helper.setReplyTo(replyToEmail);
            }
            message.saveChanges();

            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            message.writeTo(buffer);
            byte[] rawBytes = buffer.toByteArray();
            String encodedEmail = java.util.Base64.getUrlEncoder().encodeToString(rawBytes);

            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("raw", encodedEmail);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> request = new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            restTemplate.postForEntity("https://gmail.googleapis.com/gmail/v1/users/me/messages/send", request, String.class);
            log.info("Gmail API sent email successfully to: {}, subject: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("Error sending email via Gmail REST API: {}", e.getMessage(), e);
            throw new MessagingException("Gmail REST API error: " + e.getMessage());
        }
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent)
            throws MessagingException {
        if (gmailApiEnabled && isGmailApiConfigured()) {
            sendEmailViaGmailApi(toEmail, subject, htmlContent, null, null);
        } else {
            sendEmailViaBrevo(toEmail, subject, htmlContent, null, null);
        }
    }

    private void sendHtmlEmailWithSender(String toEmail, String subject, String htmlContent, String senderEmail, String senderName)
            throws MessagingException {
        if (gmailApiEnabled && isGmailApiConfigured()) {
            sendEmailViaGmailApi(toEmail, subject, htmlContent, senderEmail, senderName);
        } else {
            sendEmailViaBrevo(toEmail, subject, htmlContent, senderEmail, senderName);
        }
    }

    private void sendEmailViaBrevo(String toEmail, String subject, String htmlContent, String replyToEmail, String replyToName) throws MessagingException {
        String apiKey = brevoApiKey != null ? brevoApiKey.trim() : null;
        if (apiKey == null || apiKey.isEmpty()) {
            log.info("Không tìm thấy cấu hình Brevo API Key, tự động chuyển về gửi qua SMTP chuẩn...");
            sendHtmlEmailSmtp(toEmail, subject, htmlContent, replyToEmail, replyToName);
            return;
        }

        if (!apiKey.startsWith("xkeysib-")) {
            throw new MessagingException("LỖI CẤU HÌNH: Mã Brevo API Key không hợp lệ. Mã đúng phải bắt đầu bằng chữ 'xkeysib-'. Vui lòng không nhầm lẫn với mã SMTP (xsmtpsib-).");
        }

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("sender", java.util.Map.of("name", fromName, "email", fromEmail));
            body.put("to", java.util.List.of(java.util.Map.of("email", toEmail)));
            body.put("subject", subject);
            body.put("htmlContent", htmlContent);
            
            if (replyToEmail != null && !replyToEmail.trim().isEmpty()) {
                body.put("replyTo", java.util.Map.of("email", replyToEmail, "name", replyToName != null ? replyToName : replyToEmail));
            }

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> request = new org.springframework.http.HttpEntity<>(body, headers);
            
            restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", request, String.class);
            log.info("Brevo API đã gửi email thành công: to={}, subject={}", toEmail, subject);
        } catch (Exception e) {
            log.error("Lỗi khi gửi mail qua Brevo API: {}", e.getMessage(), e);
            throw new MessagingException("Lỗi Brevo API: " + e.getMessage());
        }
    }

    private void sendHtmlEmailSmtp(String toEmail, String subject, String htmlContent, String replyToEmail, String replyToName)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(fromEmail, fromName);
            if (replyToEmail != null && !replyToEmail.trim().isEmpty()) {
                helper.setReplyTo(replyToEmail);
            }
        } catch (UnsupportedEncodingException e) {
            log.warn("setFrom fallback: {}", e.getMessage());
            helper.setFrom(fromEmail);
        }

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setValidateAddresses(true);
        message.saveChanges();
        mailSender.send(message);
        log.info("SMTP accepted email: from={}, replyTo={}, to={}, subject={}, messageId={}",
                fromEmail, replyToEmail, toEmail, subject, message.getMessageID());
    }
}
