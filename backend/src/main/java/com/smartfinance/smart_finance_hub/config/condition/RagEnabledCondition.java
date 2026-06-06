package com.smartfinance.smart_finance_hub.config.condition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Bean condition: RAG beans chỉ tạo khi cả AI enabled VÀ ai.rag.enabled=true.
 * Cho phép dùng AI chatbot mà không bắt buộc có RAG.
 */
@Slf4j
public class RagEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        AiEnabledCondition aiCondition = new AiEnabledCondition();
        boolean aiEnabled = aiCondition.matches(context, metadata);

        String ragEnabled = context.getEnvironment().getProperty("ai.rag.enabled", "false");
        boolean result = aiEnabled && "true".equalsIgnoreCase(ragEnabled);

        log.info("RagEnabledCondition: aiEnabled={}, ragEnabled={}, result={}", aiEnabled, ragEnabled, result);

        return result;
    }
}
