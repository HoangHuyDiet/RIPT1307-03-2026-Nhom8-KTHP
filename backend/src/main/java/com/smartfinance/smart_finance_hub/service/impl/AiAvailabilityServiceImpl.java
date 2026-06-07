package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.response.AiAvailabilityDTO;
import com.smartfinance.smart_finance_hub.dto.response.RagStatusDTO;
import com.smartfinance.smart_finance_hub.enums.AiErrorCode;
import com.smartfinance.smart_finance_hub.service.AiAvailabilityService;
import com.smartfinance.smart_finance_hub.service.AiModelClient;
import com.smartfinance.smart_finance_hub.service.KnowledgeIngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AiAvailabilityServiceImpl implements AiAvailabilityService {

    private final Optional<AiModelClient> aiModelClient;
    private final Optional<KnowledgeIngestionService> ingestionService;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${ai.rag.enabled:false}")
    private boolean ragEnabled;

    @Autowired
    public AiAvailabilityServiceImpl(
            @Autowired(required = false) AiModelClient aiModelClient,
            @Autowired(required = false) KnowledgeIngestionService ingestionService) {
        this.aiModelClient = Optional.ofNullable(aiModelClient);
        this.ingestionService = Optional.ofNullable(ingestionService);
    }

    @Override
    public AiAvailabilityDTO getAvailability() {
        boolean modelAvailable = aiModelClient.map(AiModelClient::isAvailable).orElse(false);
        boolean aiAvailable = aiEnabled && modelAvailable;
        RagStatusDTO ragStatus = readRagStatus();

        return AiAvailabilityDTO.builder()
            .aiEnabled(aiEnabled)
            .aiAvailable(aiAvailable)
            .ragEnabled(ragEnabled)
            .ragAvailable(ragStatus != null && ragStatus.isRagAvailable())
            .rebuildStatus(ragStatus != null ? ragStatus.getRebuildStatus() : null)
            .knowledgeBaseHash(ragStatus != null && ragStatus.getKnowledgeBaseHash() != null
                ? ragStatus.getKnowledgeBaseHash()
                : "")
            .errorCode(resolveErrorCode(aiAvailable, modelAvailable))
            .reason(buildReason(aiAvailable, modelAvailable, ragStatus))
            .build();
    }

    private RagStatusDTO readRagStatus() {
        if (ingestionService.isEmpty()) {
            return null;
        }
        try {
            return ingestionService.get().getStatus();
        } catch (Exception e) {
            log.warn("Unable to read RAG availability: {}", e.getMessage());
            return null;
        }
    }

    private AiErrorCode resolveErrorCode(boolean aiAvailable, boolean modelAvailable) {
        if (aiAvailable) {
            return null;
        }
        return aiEnabled && !modelAvailable ? AiErrorCode.AI_UNAVAILABLE : AiErrorCode.AI_DISABLED;
    }

    private String buildReason(boolean aiAvailable, boolean modelAvailable, RagStatusDTO ragStatus) {
        if (aiEnabled && !modelAvailable) {
            return "AI is enabled but model client is unavailable";
        }
        if (!aiAvailable) {
            return "AI is disabled";
        }
        if (ragEnabled && (ragStatus == null || !ragStatus.isRagAvailable())) {
            return "AI is available; RAG is not ready";
        }
        return "AI is available";
    }
}
