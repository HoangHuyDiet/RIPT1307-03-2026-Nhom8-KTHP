package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.SubscriptionCheckoutRequest;
import com.smartfinance.smart_finance_hub.dto.response.SubscriptionCheckoutResponse;
import com.smartfinance.smart_finance_hub.dto.response.SubscriptionPlanResponse;
import com.smartfinance.smart_finance_hub.dto.response.UserSubscriptionResponse;
import java.util.List;
import java.util.Map;

public interface SubscriptionService {

    List<SubscriptionPlanResponse> getActivePlans();

    UserSubscriptionResponse getCurrentSubscription(Long userId);

    SubscriptionCheckoutResponse createCheckout(Long userId, SubscriptionCheckoutRequest request);

    SubscriptionCheckoutResponse getOrder(Long userId, Long orderCode);

    void handlePayosWebhook(Map<String, Object> payload);
}
