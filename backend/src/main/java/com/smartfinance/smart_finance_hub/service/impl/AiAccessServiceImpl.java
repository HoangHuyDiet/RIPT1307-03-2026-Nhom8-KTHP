package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.enums.SubscriptionStatus;
import com.smartfinance.smart_finance_hub.repository.UserSubscriptionRepository;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.AiAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AiAccessServiceImpl implements AiAccessService {

    private final UserSubscriptionRepository subscriptionRepository;

    @Value("${ai.access.require-pro:false}")
    private boolean proRequired;

    @Value("#{'${ai.access.allowed-roles:PRO,VIP,ADMIN,SUPPORT_ADMIN}'.split(',')}")
    private List<String> allowedRoles;

    @Value("${ai.access.upgrade-message:Vui lòng nâng cấp gói Pro để sử dụng trợ lý AI tài chính.}")
    private String upgradeMessage;

    @Override
    public boolean canUseAi(CustomUserDetails userDetails) {
        if (!proRequired) {
            return true;
        }
        if (userDetails == null || userDetails.getAuthorities() == null) {
            return false;
        }
        boolean hasAllowedRole = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(this::normalizeAuthority)
            .anyMatch(this::isAllowedRole);
        if (hasAllowedRole) {
            return true;
        }
        return subscriptionRepository
            .findFirstByUserIdAndStatusAndExpiredAtAfterOrderByExpiredAtDesc(
                userDetails.getId(),
                SubscriptionStatus.ACTIVE,
                LocalDateTime.now()
            )
            .isPresent();
    }

    @Override
    public boolean isProRequired() {
        return proRequired;
    }

    @Override
    public List<String> getAllowedRoles() {
        return allowedRoles.stream()
            .map(this::normalizeRole)
            .distinct()
            .toList();
    }

    @Override
    public String getUpgradeMessage() {
        return upgradeMessage;
    }

    private boolean isAllowedRole(String role) {
        return getAllowedRoles().contains(role);
    }

    private String normalizeAuthority(String authority) {
        String normalized = normalizeRole(authority);
        return normalized.startsWith("ROLE_") ? normalized.substring("ROLE_".length()) : normalized;
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }
}
