package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.entity.Notification;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.repository.NotificationRepository;
import com.smartfinance.smart_finance_hub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyNotifications(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdAndIsDeletedFalseOrderByIdDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Notification n : list) {
            result.add(convertToMap(n));
        }
        return result;
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findByIdAndUserId(notificationId, userId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdAndIsDeletedFalseOrderByIdDesc(userId);
        for (Notification n : list) {
            if (!n.getRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.findByIdAndUserId(notificationId, userId).ifPresent(n -> {
            n.setIsDeleted(true);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void deleteAllNotifications(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdAndIsDeletedFalseOrderByIdDesc(userId);
        for (Notification n : list) {
            n.setIsDeleted(true);
            notificationRepository.save(n);
        }
    }

    @Override
    @Transactional
    public void createAndSendNotification(User user, String type, Long fundId, String fundName,
                                           BigDecimal amount, String description, String requesterName,
                                           String bankAccount, String bankName, String targetRole, String linkAction) {
        Notification n = Notification.builder()
                .user(user)
                .type(type)
                .fundId(fundId)
                .fundName(fundName)
                .amount(amount)
                .description(description)
                .requesterName(requesterName)
                .bankAccount(bankAccount)
                .bankName(bankName)
                .read(false)
                .isDeleted(false)
                .targetRole(targetRole)
                .linkAction(linkAction)
                .build();

        Notification saved = notificationRepository.save(n);
        Map<String, Object> map = convertToMap(saved);

        try {
            if (user.getEmail() != null) {
                messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", map);
            }
        } catch (Exception e) {
            log.error("Failed to send real-time notification via WebSocket", e);
        }
    }

    private Map<String, Object> convertToMap(Notification n) {
        Map<String, Object> map = new HashMap<>();
        String prefix = getPrefix(n.getType(), n.getLinkAction());
        map.put("id", prefix + n.getId());
        map.put("type", n.getType());
        map.put("fundId", n.getFundId());
        map.put("fundName", n.getFundName());
        map.put("amount", n.getAmount() != null ? n.getAmount() : BigDecimal.ZERO);
        map.put("description", n.getDescription());
        map.put("requesterName", n.getRequesterName() != null ? n.getRequesterName() : "");
        map.put("bankAccount", n.getBankAccount());
        map.put("bankName", n.getBankName());
        map.put("date", n.getCreatedAt() != null ? n.getCreatedAt().toString() : LocalDateTime.now().toString());
        map.put("read", n.getRead());
        map.put("targetRole", n.getTargetRole() != null ? n.getTargetRole() : "MEMBER");
        map.put("link_action", n.getLinkAction());
        return map;
    }

    private String getPrefix(String type, String linkAction) {
        if ("DEPOSIT_REQUEST".equals(type) || "WITHDRAW_REQUEST".equals(type)) {
            return "fund_req_";
        }
        if ("DEPOSIT_APPROVED".equals(type) || "WITHDRAW_APPROVED".equals(type)) {
            return "fund_approved_";
        }
        if ("DEPOSIT_REJECTED".equals(type) || "WITHDRAW_REJECTED".equals(type)) {
            return "fund_rejected_";
        }
        if ("FUND_INVITATION".equals(type)) {
            return "fund_invitation_";
        }
        if ("FUND_DISBAND_PROPOSAL".equals(type)) {
            return "fund_disband_";
        }
        if ("FUND_MEMBER_REMOVED".equals(type)) {
            return "fund_removed_";
        }
        if (linkAction != null) {
            if (linkAction.contains("saving-goals")) {
                return "goal_";
            }
            if (linkAction.contains("personal-funds")) {
                return "pf_";
            }
        }
        if ("SYSTEM_INFO".equals(type)) {
            return "dash_";
        }
        return "fund_";
    }
}
