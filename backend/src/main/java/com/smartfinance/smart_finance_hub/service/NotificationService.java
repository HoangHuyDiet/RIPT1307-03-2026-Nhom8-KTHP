package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.entity.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface NotificationService {

    List<Map<String, Object>> getMyNotifications(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long notificationId, Long userId);

    void deleteAllNotifications(Long userId);

    void createAndSendNotification(User user, String type, Long fundId, String fundName,
                                   BigDecimal amount, String description, String requesterName,
                                   String bankAccount, String bankName, String targetRole, String linkAction);
}
