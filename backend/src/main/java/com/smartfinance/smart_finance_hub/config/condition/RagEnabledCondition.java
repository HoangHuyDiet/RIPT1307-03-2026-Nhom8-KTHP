package com.smartfinance.smart_finance_hub.config.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RagEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean aiEnabled = new AiEnabledCondition().matches(context, metadata);
        String ragEnabled = context.getEnvironment().getProperty("ai.rag.enabled", "false");
        return aiEnabled && "true".equalsIgnoreCase(ragEnabled);
    }
}
