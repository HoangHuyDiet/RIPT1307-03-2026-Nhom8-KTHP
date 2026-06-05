package com.smartfinance.smart_finance_hub.config;

import com.smartfinance.smart_finance_hub.config.condition.AiEnabledCondition;
import com.smartfinance.smart_finance_hub.config.condition.RagEnabledCondition;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${gemini.embedding-model:gemini-embedding-001}")
    private String embeddingModel;

    @Value("${gemini.temperature:0.2}")
    private Double temperature;

    @Bean
    @Conditional(AiEnabledCondition.class)
    public GoogleAiGeminiChatModel geminiChatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(geminiModel)
                .temperature(temperature)
                .build();
    }

    @Bean
    @Conditional(RagEnabledCondition.class)
    public GoogleAiEmbeddingModel geminiEmbeddingModel() {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName(embeddingModel)
                .build();
    }

    @Bean
    @Conditional(RagEnabledCondition.class)
    public InMemoryEmbeddingStore<TextSegment> inMemoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}
