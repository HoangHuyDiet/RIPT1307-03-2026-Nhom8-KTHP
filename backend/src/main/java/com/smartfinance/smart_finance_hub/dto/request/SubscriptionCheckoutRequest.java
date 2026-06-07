package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionCheckoutRequest {

    @NotBlank(message = "Plan code is required")
    private String planCode;

    private String couponCode;
}
