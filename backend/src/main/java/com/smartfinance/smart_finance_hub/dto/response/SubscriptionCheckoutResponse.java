package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.entity.PaymentOrder;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionCheckoutResponse {

    private Long orderCode;
    private String status;
    private String planCode;
    private String planName;
    private BigDecimal amount;
    private String description;
    private String checkoutUrl;
    private String qrCode;
    private String paymentLinkId;

    public static SubscriptionCheckoutResponse from(PaymentOrder order) {
        return SubscriptionCheckoutResponse.builder()
            .orderCode(order.getOrderCode())
            .status(order.getStatus().name())
            .planCode(order.getPlan().getCode())
            .planName(order.getPlan().getName())
            .amount(order.getAmount())
            .description(order.getDescription())
            .checkoutUrl(order.getCheckoutUrl())
            .qrCode(order.getQrCode())
            .paymentLinkId(order.getPayosPaymentLinkId())
            .build();
    }
}
