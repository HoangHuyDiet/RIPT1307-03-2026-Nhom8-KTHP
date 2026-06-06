package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.SubscriptionCheckoutRequest;
import com.smartfinance.smart_finance_hub.dto.response.SubscriptionCheckoutResponse;
import com.smartfinance.smart_finance_hub.dto.response.SubscriptionPlanResponse;
import com.smartfinance.smart_finance_hub.dto.response.UserSubscriptionResponse;
import com.smartfinance.smart_finance_hub.entity.PaymentOrder;
import com.smartfinance.smart_finance_hub.entity.Role;
import com.smartfinance.smart_finance_hub.entity.SubscriptionPlan;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.entity.UserRole;
import com.smartfinance.smart_finance_hub.entity.UserSubscription;
import com.smartfinance.smart_finance_hub.enums.PaymentOrderStatus;
import com.smartfinance.smart_finance_hub.enums.SubscriptionStatus;
import com.smartfinance.smart_finance_hub.repository.PaymentOrderRepository;
import com.smartfinance.smart_finance_hub.repository.RoleRepository;
import com.smartfinance.smart_finance_hub.repository.SubscriptionPlanRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.repository.UserRoleRepository;
import com.smartfinance.smart_finance_hub.repository.UserSubscriptionRepository;
import com.smartfinance.smart_finance_hub.service.SubscriptionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    private final RestClient restClient = RestClient.create();

    @Value("${payos.api-base-url:https://api-merchant.payos.vn}")
    private String payosBaseUrl;

    @Value("${payos.client-id:}")
    private String payosClientId;

    @Value("${payos.api-key:}")
    private String payosApiKey;

    @Value("${payos.checksum-key:}")
    private String payosChecksumKey;

    @Value("${payos.return-url:${app.frontend-url:http://localhost:8000}/pricing?payment=success}")
    private String returnUrl;

    @Value("${payos.cancel-url:${app.frontend-url:http://localhost:8000}/pricing?payment=cancelled}")
    private String cancelUrl;

    @Value("${payos.webhook-url:http://localhost:8080/backend/api/payments/payos/webhook}")
    private String webhookUrl;

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getActivePlans() {
        return planRepository.findByActiveTrueOrderByPriceAsc().stream()
            .map(SubscriptionPlanResponse::from)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserSubscriptionResponse getCurrentSubscription(Long userId) {
        return subscriptionRepository
            .findFirstByUserIdAndStatusAndExpiredAtAfterOrderByExpiredAtDesc(
                userId,
                SubscriptionStatus.ACTIVE,
                LocalDateTime.now()
            )
            .map(UserSubscriptionResponse::from)
            .orElseGet(UserSubscriptionResponse::free);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public SubscriptionCheckoutResponse createCheckout(Long userId, SubscriptionCheckoutRequest request) {
        ensurePayosConfigured();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi dung!"));
        SubscriptionPlan plan = planRepository.findByCode(normalizePlanCode(request.getPlanCode()))
            .filter(SubscriptionPlan::getActive)
            .orElseThrow(() -> new IllegalArgumentException("Goi Pro khong ton tai hoac da bi tat!"));

        BigDecimal amount = applyCoupon(plan.getPrice(), request.getCouponCode());
        long orderCode = generateOrderCode();
        String description = "SFM PRO " + orderCode;

        PaymentOrder order = PaymentOrder.builder()
            .orderCode(orderCode)
            .user(user)
            .plan(plan)
            .amount(amount)
            .status(PaymentOrderStatus.PENDING)
            .description(description)
            .build();
        paymentOrderRepository.save(order);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderCode", orderCode);
        body.put("amount", toPayosAmount(amount));
        body.put("description", description);
        body.put("cancelUrl", cancelUrl);
        body.put("returnUrl", returnUrl);
        body.put("items", List.of(Map.of(
            "name", plan.getName(),
            "quantity", 1,
            "price", toPayosAmount(amount)
        )));
        body.put("signature", signCreatePaymentRequest(body));

        Map<String, Object> response = restClient.post()
            .uri(payosBaseUrl + "/v2/payment-requests")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-client-id", payosClientId)
            .header("x-api-key", payosApiKey)
            .body(body)
            .retrieve()
            .body(Map.class);

        log.info("PayOS response for orderCode={}: {}", orderCode, response);

        Map<String, Object> data = response != null ? (Map<String, Object>) response.get("data") : null;
        if (data == null) {
            String desc = response != null ? String.valueOf(response.get("desc")) : "Null response";
            String code = response != null ? String.valueOf(response.get("code")) : "Null";
            throw new IllegalStateException("Loi tu PayOS (code " + code + "): " + desc);
        }

        order.setCheckoutUrl(asString(data.get("checkoutUrl")));
        order.setQrCode(asString(data.get("qrCode")));
        order.setPayosPaymentLinkId(asString(data.get("paymentLinkId")));
        paymentOrderRepository.save(order);

        return SubscriptionCheckoutResponse.from(order);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public SubscriptionCheckoutResponse getOrder(Long userId, Long orderCode) {
        PaymentOrder order = paymentOrderRepository.findByOrderCode(orderCode)
            .filter(o -> o.getUser().getId().equals(userId))
            .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don thanh toan!"));

        if (PaymentOrderStatus.PENDING.equals(order.getStatus())) {
            try {
                ensurePayosConfigured();
                Map<String, Object> response = restClient.get()
                    .uri(payosBaseUrl + "/v2/payment-requests/" + orderCode)
                    .header("x-client-id", payosClientId)
                    .header("x-api-key", payosApiKey)
                    .retrieve()
                    .body(Map.class);

                if (response != null && "00".equals(asString(response.get("code")))) {
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    if (data != null) {
                        String payosStatus = asString(data.get("status"));
                        if ("PAID".equalsIgnoreCase(payosStatus)) {
                            order.setStatus(PaymentOrderStatus.PAID);
                            order.setPaidAt(LocalDateTime.now());

                            List<Map<String, Object>> transactions = (List<Map<String, Object>>) data.get("transactions");
                            if (transactions != null && !transactions.isEmpty()) {
                                Map<String, Object> lastTx = transactions.get(transactions.size() - 1);
                                order.setPayosReference(asString(lastTx.get("reference")));
                            }

                            paymentOrderRepository.save(order);
                            activateSubscription(order);
                            log.info("Chu dong dong bo thanh cong don hang PayOS orderCode={} sang trang thai PAID", orderCode);
                        } else if ("CANCELLED".equalsIgnoreCase(payosStatus)) {
                            order.setStatus(PaymentOrderStatus.FAILED);
                            paymentOrderRepository.save(order);
                            log.info("Chu dong dong bo don hang PayOS orderCode={} sang trang thai FAILED", orderCode);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Loi khi chu dong dong bo trang thai don hang tu PayOS: {}", e.getMessage());
            }
        }

        return SubscriptionCheckoutResponse.from(order);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void handlePayosWebhook(Map<String, Object> payload) {
        Object dataObject = payload.get("data");
        if (!(dataObject instanceof Map<?, ?> rawData)) {
            throw new IllegalArgumentException("Webhook PayOS khong co data hop le");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        rawData.forEach((key, value) -> data.put(String.valueOf(key), value));

        String signature = asString(payload.get("signature"));
        if (!verifySignature(data, signature)) {
            throw new IllegalArgumentException("Chu ky webhook PayOS khong hop le");
        }

        Long orderCode = toLong(data.get("orderCode"));
        if (orderCode == 123) {
            log.info("Nhan webhook test tu PayOS (orderCode=123). Xac thuc thanh cong.");
            return;
        }

        java.util.Optional<PaymentOrder> orderOpt = paymentOrderRepository.findByOrderCode(orderCode);
        if (orderOpt.isEmpty()) {
            log.warn("Khong tim thay don PayOS: {}. Tra ve OK de bypass validation.", orderCode);
            return;
        }
        PaymentOrder order = orderOpt.get();

        if (PaymentOrderStatus.PAID.equals(order.getStatus())) {
            return;
        }

        BigDecimal paidAmount = new BigDecimal(String.valueOf(data.get("amount")));
        if (order.getAmount().compareTo(paidAmount) != 0) {
            order.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(order);
            throw new IllegalArgumentException("So tien thanh toan khong khop voi don hang");
        }

        String paymentCode = asString(data.get("code"));
        if (!"00".equals(paymentCode)) {
            order.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(order);
            return;
        }

        order.setStatus(PaymentOrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setPayosReference(asString(data.get("reference")));
        paymentOrderRepository.save(order);

        activateSubscription(order);
    }

    private void activateSubscription(PaymentOrder order) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseStart = subscriptionRepository
            .findFirstByUserIdAndStatusAndExpiredAtAfterOrderByExpiredAtDesc(
                order.getUser().getId(),
                SubscriptionStatus.ACTIVE,
                now
            )
            .map(UserSubscription::getExpiredAt)
            .filter(expiredAt -> expiredAt.isAfter(now))
            .orElse(now);

        LocalDateTime expiredAt = baseStart.plusDays(order.getPlan().getDurationDays());
        UserSubscription subscription = UserSubscription.builder()
            .user(order.getUser())
            .plan(order.getPlan())
            .status(SubscriptionStatus.ACTIVE)
            .startedAt(now)
            .expiredAt(expiredAt)
            .build();
        subscriptionRepository.save(subscription);

        Role proRole = roleRepository.findByName("PRO")
            .orElseThrow(() -> new IllegalStateException("Role PRO chua duoc khoi tao"));
        UserRole userRole = userRoleRepository
            .findByUserIdAndRoleId(order.getUser().getId(), proRole.getId())
            .orElseGet(() -> UserRole.builder()
                .user(order.getUser())
                .role(proRole)
                .build());
        userRole.setExpiredAt(expiredAt);
        userRoleRepository.save(userRole);

        log.info("Activated Pro subscription for userId={}, plan={}, expiredAt={}",
            order.getUser().getId(), order.getPlan().getCode(), expiredAt);
    }

    private String signCreatePaymentRequest(Map<String, Object> body) {
        Map<String, Object> signatureData = new TreeMap<>();
        signatureData.put("amount", body.get("amount"));
        signatureData.put("cancelUrl", body.get("cancelUrl"));
        signatureData.put("description", body.get("description"));
        signatureData.put("orderCode", body.get("orderCode"));
        signatureData.put("returnUrl", body.get("returnUrl"));
        return hmacSha256(toSignatureString(signatureData), payosChecksumKey);
    }

    private boolean verifySignature(Map<String, Object> data, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        String expected = hmacSha256(toSignatureString(new TreeMap<>(data)), payosChecksumKey);
        return expected.equalsIgnoreCase(signature);
    }

    private String toSignatureString(Map<String, Object> data) {
        List<String> parts = new ArrayList<>();
        data.forEach((key, value) -> {
            String valStr;
            if (value == null) {
                valStr = "";
            } else if (value instanceof Double d) {
                if (d % 1 == 0) {
                    valStr = String.format(Locale.ROOT, "%.0f", d);
                } else {
                    valStr = String.valueOf(d);
                }
            } else if (value instanceof Float f) {
                if (f % 1 == 0) {
                    valStr = String.format(Locale.ROOT, "%.0f", f);
                } else {
                    valStr = String.valueOf(f);
                }
            } else if (value instanceof BigDecimal bd) {
                BigDecimal stripped = bd.stripTrailingZeros();
                if (stripped.scale() <= 0) {
                    valStr = stripped.toBigInteger().toString();
                } else {
                    valStr = stripped.toPlainString();
                }
            } else {
                valStr = String.valueOf(value);
            }
            parts.add(key + "=" + valStr);
        });
        return String.join("&", parts);
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Khong the tao chu ky PayOS", e);
        }
    }

    private BigDecimal applyCoupon(BigDecimal price, String couponCode) {
        if ("WELCOME50".equalsIgnoreCase(couponCode == null ? "" : couponCode.trim())) {
            return price.multiply(BigDecimal.valueOf(0.5)).setScale(0, RoundingMode.HALF_UP);
        }
        return price.setScale(0, RoundingMode.HALF_UP);
    }

    private int toPayosAmount(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.UNNECESSARY).intValueExact();
    }

    private long generateOrderCode() {
        return Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(3));
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String normalizePlanCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private void ensurePayosConfigured() {
        if (payosClientId.isBlank() || payosApiKey.isBlank() || payosChecksumKey.isBlank()) {
            throw new IllegalStateException("Chua cau hinh PAYOS_CLIENT_ID, PAYOS_API_KEY hoac PAYOS_CHECKSUM_KEY");
        }
    }
}
