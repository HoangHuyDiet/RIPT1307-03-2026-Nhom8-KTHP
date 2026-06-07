package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.entity.UserSubscription;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSubscriptionResponse {

    private String status;
    private String planCode;
    private String planName;
    private LocalDateTime startedAt;
    private LocalDateTime expiredAt;
    private boolean active;

    public static UserSubscriptionResponse free() {
        return UserSubscriptionResponse.builder()
            .status("FREE")
            .planCode("FREE")
            .planName("Goi thuong")
            .active(false)
            .build();
    }

    public static UserSubscriptionResponse from(UserSubscription subscription) {
        boolean active = subscription.isActiveNow();
        return UserSubscriptionResponse.builder()
            .status(subscription.getStatus().name())
            .planCode(subscription.getPlan().getCode())
            .planName(subscription.getPlan().getName())
            .startedAt(subscription.getStartedAt())
            .expiredAt(subscription.getExpiredAt())
            .active(active)
            .build();
    }
}
