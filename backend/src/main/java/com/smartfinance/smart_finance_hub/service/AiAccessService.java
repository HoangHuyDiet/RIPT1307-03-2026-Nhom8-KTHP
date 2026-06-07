package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.security.CustomUserDetails;

import java.util.List;

public interface AiAccessService {

    boolean canUseAi(CustomUserDetails userDetails);

    boolean isProRequired();

    List<String> getAllowedRoles();

    String getUpgradeMessage();
}
