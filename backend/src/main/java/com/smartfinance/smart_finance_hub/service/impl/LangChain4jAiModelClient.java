package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.config.condition.AiEnabledCondition;
import com.smartfinance.smart_finance_hub.service.AiModelClient;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Service
@Conditional(AiEnabledCondition.class)
public class LangChain4jAiModelClient implements AiModelClient {

    private final ChatModel chatModel;

    public LangChain4jAiModelClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String generate(String prompt) {
        return chatModel.chat(prompt);
    }
}
