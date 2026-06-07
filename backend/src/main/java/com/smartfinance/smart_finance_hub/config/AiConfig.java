package com.smartfinance.smart_finance_hub.config;

import com.smartfinance.smart_finance_hub.config.condition.AiEnabledCondition;
import com.smartfinance.smart_finance_hub.config.condition.RagEnabledCondition;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Slf4j
public class AiConfig {

    @Bean
    @Conditional(AiEnabledCondition.class)
    public ChatModel chatLanguageModel(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model,
            @Value("${gemini.temperature}") double temperature) {
        log.info("Initializing Gemini ChatModel: model={}, temperature={}", model, temperature);
        return GoogleAiGeminiChatModel.builder()
            .apiKey(apiKey)
            .modelName(model)
            .temperature(temperature)
            .build();
    }

    @Bean
    @Conditional(RagEnabledCondition.class)
    public EmbeddingModel embeddingModel(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.embedding-model}") String embeddingModelName) {
        log.info("Initializing Gemini EmbeddingModel: {}", embeddingModelName);
        return GoogleAiEmbeddingModel.builder()
            .apiKey(apiKey)
            .modelName(embeddingModelName)
            .build();
    }

    @Bean
    @Conditional(RagEnabledCondition.class)
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("Initializing InMemoryEmbeddingStore");
        return new InMemoryEmbeddingStore<>();
    }

    @Bean(name = "ragRebuildExecutor")
    @Conditional(RagEnabledCondition.class)
    public Executor ragRebuildExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("rag-rebuild-");
        executor.initialize();
        return executor;
    }
}
