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
        helper.setValidateAddresses(true);
        message.saveChanges();
        mailSender.send(message);
        log.info("SMTP accepted email: from={}, to={}, subject={}, messageId={}",
                fromEmail, toEmail, subject, message.getMessageID());
    }

    private void sendHtmlEmailWithSender(String toEmail, String subject, String htmlContent, String senderEmail, String senderName)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(fromEmail, fromName);
            if (senderEmail != null && !senderEmail.trim().isEmpty()) {
                helper.setReplyTo(senderEmail);
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
                fromEmail, senderEmail, toEmail, subject, message.getMessageID());
    }
}
