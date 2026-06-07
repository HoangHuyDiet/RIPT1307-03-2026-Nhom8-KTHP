package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.entity.SubscriptionPlan;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPlanResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Boolean active;

    public static SubscriptionPlanResponse from(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
            .id(plan.getId())
            .code(plan.getCode())
            .name(plan.getName())
            .description(plan.getDescription())
            .price(plan.getPrice())
            .durationDays(plan.getDurationDays())
            .active(plan.getActive())
            .build();
    }
}
