package com.smartfinance.smart_finance_hub.service;

import jakarta.mail.MessagingException;

public interface MailService {

    void sendOtpEmail(String toEmail, String userName, String otpCode, int expiryMinutes)
            throws MessagingException;

    void sendWelcomeEmail(String toEmail, String userName) throws MessagingException;

    void sendResetPasswordEmail(String toEmail, String userName, String resetCode, int expiryMinutes)
            throws MessagingException;

    void sendFundInvitationEmail(String toEmail, String userName, String invitedByName, String invitedByEmail,
            String fundName, String token, int expiryHours) throws MessagingException;

    void sendKickProposalEmail(String toEmail, String userName, String fundName,
            String kickedByName, String reason, String token) throws MessagingException;

    void sendDisbandProposalEmail(String toEmail, String userName, String fundName,
            String proposedByName, String proposedByEmail, String reason, String token) throws MessagingException;

    void sendDisbandConfirmationEmail(String toEmail, String userName, String fundName)
            throws MessagingException;

    void sendFundNotificationEmail(String toEmail, String userName, String fundName,
            String subject, String content) throws MessagingException;
}


