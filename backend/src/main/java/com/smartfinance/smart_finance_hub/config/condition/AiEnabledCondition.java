package com.smartfinance.smart_finance_hub.config.condition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Bean condition: chỉ tạo AI beans khi ai.enabled=true VÀ GEMINI_API_KEY không rỗng.
 * Đảm bảo app luôn boot an toàn khi thiếu API key.
 */
@Slf4j
public class AiEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String aiEnabled = context.getEnvironment().getProperty("ai.enabled", "false");
        String apiKey = context.getEnvironment().getProperty("gemini.api-key", "");

        boolean result = "true".equalsIgnoreCase(aiEnabled) && apiKey != null && !apiKey.isBlank();

        if ("true".equalsIgnoreCase(aiEnabled) && !result) {
            log.warn("ai.enabled=true nhưng GEMINI_API_KEY rỗng — AI beans sẽ KHÔNG được tạo");
        }

        log.info("AiEnabledCondition: aiEnabled={}, hasApiKey={}, result={}",
            aiEnabled, apiKey != null && !apiKey.isBlank(), result);

        return result;
    }
}
