package com.smartfinance.smart_finance_hub.config;

import com.smartfinance.smart_finance_hub.entity.SubscriptionPlan;
import com.smartfinance.smart_finance_hub.repository.SubscriptionPlanRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanInitializer implements ApplicationRunner {

    private final SubscriptionPlanRepository planRepository;

    @Override
    public void run(ApplicationArguments args) {
        createIfMissing("PRO_MONTHLY", "Pro 1 thang",
            "Mo khoa tro ly AI tai chinh trong 30 ngay", BigDecimal.valueOf(59000), 30);
        createIfMissing("PRO_SEMIANNUAL", "Pro 6 thang",
            "Mo khoa tro ly AI tai chinh trong 180 ngay", BigDecimal.valueOf(299000), 180);
        createIfMissing("PRO_ANNUAL", "Pro 1 nam",
            "Mo khoa tro ly AI tai chinh trong 365 ngay", BigDecimal.valueOf(499000), 365);
    }

    private void createIfMissing(String code, String name, String description, BigDecimal price, int durationDays) {
        if (planRepository.findByCode(code).isPresent()) {
            return;
        }
        planRepository.save(SubscriptionPlan.builder()
            .code(code)
            .name(name)
            .description(description)
            .price(price)
            .durationDays(durationDays)
            .active(true)
            .build());
    }
}
