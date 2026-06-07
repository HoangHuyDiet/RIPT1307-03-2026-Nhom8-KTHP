package com.smartfinance.smart_finance_hub.config.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class AiEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String aiEnabled = context.getEnvironment().getProperty("ai.enabled", "false");
        String geminiKey = context.getEnvironment().getProperty("gemini.api-key", "");
        return "true".equalsIgnoreCase(aiEnabled) && geminiKey != null && !geminiKey.isBlank();
    }
}
