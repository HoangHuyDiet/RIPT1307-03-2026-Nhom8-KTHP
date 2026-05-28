package com.smartfinance.smart_finance_hub.service.impl.fund;

import com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundNotificationResponse;
import com.smartfinance.smart_finance_hub.entity.Fund;
import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import com.smartfinance.smart_finance_hub.entity.FundMessage;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.repository.FundMessageRepository;
import com.smartfinance.smart_finance_hub.service.MailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundNotificationService {

    private final FundMessageRepository fundMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MailService mailService;
    private final FundAccessService access;
    private final FundMapper mapper;

    public void saveSystemMessage(Fund fund, String text) {
        FundMessage message = FundMessage.builder()
                .fund(fund)
                .type("system")
                .text(text)
                .build();
        fundMessageRepository.save(message);
    }

    public void notifyOwnerTransactionRequest(Fund fund, Transaction transaction) {
        FundAccessService.Recipient owner = access.findOwnerRecipient(fund.getId());
        if (owner == null) {
            return;
        }
        FundNotificationResponse notification = FundNotificationResponse.builder()
                .type("transaction_request")
                .title("New transaction request")
                .message(transaction.getUser().getDisplayName() + " requested "
                        + transaction.getType() + " " + mapper.formatVnd(transaction.getAmount()))
                .fundId(fund.getId())
                .requestId(transaction.getId())
                .build();
        afterCommit(() -> messagingTemplate.convertAndSendToUser(
                owner.getEmail(), "/queue/notifications", notification),
                "notifyOwnerTransaction:" + transaction.getId());
    }

    public void notifyRequesterTransactionResult(Transaction transaction, String result) {
        FundNotificationResponse notification = FundNotificationResponse.builder()
                .type("transaction_result")
                .title("Transaction request result")
                .message("Request " + mapper.formatVnd(transaction.getAmount()) + " was " + result)
                .fundId(transaction.getFund().getId())
                .requestId(transaction.getId())
                .build();
        afterCommit(() -> messagingTemplate.convertAndSendToUser(
                transaction.getUser().getEmail(), "/queue/notifications", notification),
                "notifyRequesterTransaction:" + transaction.getId());
        afterCommit(() -> messagingTemplate.convertAndSend(
                "/topic/funds/" + transaction.getFund().getId() + "/chat",
                FundDiscussionResponse.builder()
                        .id(0L)
                        .senderName("System")
                        .type("system")
                        .text(notification.getMessage())
                        .time(mapper.formatTime(java.time.LocalDateTime.now()))
                        .build()),
                "broadcastTransactionResult:" + transaction.getId());
    }

    public void notifyOwnerAfterCommit(Fund fund, String subject, String content) {
        FundAccessService.Recipient owner = access.findOwnerRecipient(fund.getId());
        if (owner == null) {
            return;
        }
        String fundName = fund.getName();
        afterCommit(() -> sendFundNotificationEmail(
                owner.getEmail(), owner.getName(), fundName, subject, content), subject);
    }

    public void afterCommit(Runnable action, String context) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            run(action, context);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                run(action, context);
            }
        });
    }

    public void sendFundInvitationEmail(
            FundInvitation invitation, User invitedUser, User inviter, Fund fund) {
        try {
            mailService.sendFundInvitationEmail(
                    invitation.getInvitedEmail(),
                    invitedUser.getDisplayName(),
                    inviter.getDisplayName(),
                    fund.getName(),
                    invitation.getInvitationToken(),
                    7);
        } catch (MessagingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void sendKickProposalEmail(
            FundInvitation invitation, User targetUser, User owner, Fund fund, String reason) {
        try {
            mailService.sendKickProposalEmail(
                    invitation.getInvitedEmail(),
                    targetUser.getDisplayName(),
                    fund.getName(),
                    owner.getDisplayName(),
                    reason,
                    invitation.getInvitationToken());
        } catch (MessagingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void sendDisbandProposalEmail(
            String toEmail, String userName, String fundName, String ownerName, String reason, String token) {
        try {
            mailService.sendDisbandProposalEmail(toEmail, userName, fundName, ownerName, reason, token);
        } catch (MessagingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void sendDisbandConfirmationEmail(FundAccessService.Recipient recipient, String fundName) {
        try {
            mailService.sendDisbandConfirmationEmail(recipient.getEmail(), recipient.getName(), fundName);
        } catch (MessagingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void sendFundNotificationEmail(
            String email, String name, String fundName, String subject, String content) {
        try {
            mailService.sendFundNotificationEmail(email, name, fundName, subject, content);
        } catch (MessagingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void run(Runnable action, String context) {
        try {
            action.run();
        } catch (Exception ex) {
            log.warn("Async notification failed [{}]: {}", context, ex.getMessage());
        }
    }
}
