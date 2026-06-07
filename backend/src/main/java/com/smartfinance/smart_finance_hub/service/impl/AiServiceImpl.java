package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.AiChatRequest;
import com.smartfinance.smart_finance_hub.dto.response.AiAvailabilityDTO;
import com.smartfinance.smart_finance_hub.dto.response.AiCitationDTO;
import com.smartfinance.smart_finance_hub.dto.response.AiChatResponse;
import com.smartfinance.smart_finance_hub.dto.response.AiInsightDTO;
import com.smartfinance.smart_finance_hub.dto.response.FinancialSnapshotDTO;
import com.smartfinance.smart_finance_hub.dto.response.KnowledgeRetrievedSegmentDTO;
import com.smartfinance.smart_finance_hub.entity.AiChatSession;
import com.smartfinance.smart_finance_hub.entity.MonthlyStatement;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.AiErrorCode;
import com.smartfinance.smart_finance_hub.enums.ChatRole;
import com.smartfinance.smart_finance_hub.repository.MonthlyStatementRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.AiAvailabilityService;
import com.smartfinance.smart_finance_hub.service.AiModelClient;
import com.smartfinance.smart_finance_hub.service.AiService;
import com.smartfinance.smart_finance_hub.service.FinancialSnapshotService;
import com.smartfinance.smart_finance_hub.service.KnowledgeRetrievalService;
import com.smartfinance.smart_finance_hub.util.AiOutputValidator;
import com.smartfinance.smart_finance_hub.util.PiiRedactor;
import com.smartfinance.smart_finance_hub.util.SnapshotHashUtil;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    private final Optional<AiModelClient> aiModelClient;
    private final Optional<KnowledgeRetrievalService> retrievalService;
    private final AiAvailabilityService availabilityService;
    private final FinancialSnapshotService snapshotService;
    private final AiChatPersistenceService chatPersistence;
    private final MonthlyStatementRepository statementRepository;
    private final UserRepository userRepository;
    private final AiPromptBuilder promptBuilder;
    private final AiFallbackFactory fallbackFactory;
    private final AiRateLimitService rateLimitService;
    private final AiAuditService auditService;

    @Value("${ai.cache.insight-ttl-minutes:30}")
    private int insightTtlMinutes;

    @Value("${ai.cache.refresh-cooldown-seconds:60}")
    private int refreshCooldownSeconds;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String aiModelName;

    @Value("${ai.prompt.version:v1.0}")
    private String promptVersion;

    private static final String DISCLAIMER = "Thông tin do AI tạo ra chỉ mang tính tham khảo. Hãy tham vấn chuyên gia tài chính trước quyết định lớn.";

    @Autowired
    public AiServiceImpl(
            @Autowired(required = false) AiModelClient aiModelClient,
            @Autowired(required = false) KnowledgeRetrievalService retrievalService,
            AiAvailabilityService availabilityService,
            FinancialSnapshotService snapshotService,
            AiChatPersistenceService chatPersistence,
            MonthlyStatementRepository statementRepository,
            UserRepository userRepository,
            AiPromptBuilder promptBuilder,
            AiFallbackFactory fallbackFactory,
            AiRateLimitService rateLimitService,
            AiAuditService auditService) {
        this.aiModelClient = Optional.ofNullable(aiModelClient);
        this.retrievalService = Optional.ofNullable(retrievalService);
        this.availabilityService = availabilityService;
        this.snapshotService = snapshotService;
        this.chatPersistence = chatPersistence;
        this.statementRepository = statementRepository;
        this.userRepository = userRepository;
        this.promptBuilder = promptBuilder;
        this.fallbackFactory = fallbackFactory;
        this.rateLimitService = rateLimitService;
        this.auditService = auditService;
    }

    @Override
    public boolean isAiEnabled() {
        return availabilityService.getAvailability().isAiAvailable();
    }

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        if (!rateLimitService.allowChat(userId)) {
            auditService.record(userId, "AI_RATE_LIMITED", "chat rate limit exceeded");
            return AiChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .reply(fallbackFactory.rateLimited())
                    .aiEnabled(isAiEnabled())
                    .ragAvailable(false)
                    .errorCode(AiErrorCode.AI_RATE_LIMITED.name())
                    .citations(Collections.emptyList())
                    .build();
        }

        chatPersistence.validateSessionAccessIfExists(request.getSessionId(), userId);

        if (!isAiEnabled()) {
            auditService.record(userId, "AI_DISABLED", "chat requested while AI unavailable");
            return AiChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .reply(fallbackFactory.aiDisabled())
                    .aiEnabled(false)
                    .ragAvailable(false)
                    .errorCode(AiErrorCode.AI_DISABLED.name())
                    .citations(Collections.emptyList())
                    .build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        AiChatSession session = chatPersistence.getOrCreateSession(sessionId, user);
        chatPersistence.saveMessage(session, user, ChatRole.USER, request.getMessage());
        auditService.record(userId, "AI_CHAT_REQUESTED", "sessionId=" + sessionId);

        String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        FinancialSnapshotDTO snapshot = snapshotService.buildFullSnapshot(userId, currentMonth);

        List<AiCitationDTO> citations = new ArrayList<>();
        String knowledgeContext = "";
        AiAvailabilityDTO availability = availabilityService.getAvailability();
        boolean ragAvailable = availability.isRagAvailable();
        if (retrievalService.isPresent() && ragAvailable) {
            try {
                List<KnowledgeRetrievedSegmentDTO> segments = retrievalService.get()
                        .retrieveRelevant(request.getMessage());
                knowledgeContext = buildKnowledgeContext(segments, citations);
            } catch (Exception e) {
                ragAvailable = false;
                log.warn("RAG retrieval failed, continuing without RAG: {}", e.getMessage());
            }
        }

        String prompt = promptBuilder.buildChatPrompt(PiiRedactor.redact(request.getMessage()), snapshot,
                knowledgeContext);

        String aiReply;
        AiErrorCode errorCode = null;
        try {
            aiReply = generate(prompt);
        } catch (Exception e) {
            log.error("Gemini chat invocation failed: {}", e.getMessage(), e);
            aiReply = fallbackFactory.aiUnavailable();
            errorCode = classifyAiError(e);
            auditService.record(userId, errorCode.name(), "chat invocation failed");
        }

        if (!AiOutputValidator.validate(aiReply)) {
            log.warn("AI output contains suspicious financial numbers for user {}", userId);
            auditService.record(userId, "AI_OUTPUT_REJECTED", "chat output rejected by validator");
            aiReply = retryWithCorrection(prompt, snapshot);
            if (!AiOutputValidator.validate(aiReply)) {
                aiReply = fallbackFactory.outputValidationFailed(snapshot);
                errorCode = AiErrorCode.OUTPUT_VALIDATION_FAILED;
            }
        }

        chatPersistence.saveMessage(session, user, ChatRole.AI, aiReply);

        return AiChatResponse.builder()
                .sessionId(sessionId)
                .reply(aiReply)
                .aiEnabled(errorCode == null)
                .ragAvailable(ragAvailable)
                .errorCode(errorCode != null ? errorCode.name() : null)
                .disclaimer(DISCLAIMER)
                .citations(citations)
                .build();
    }

    @Override
    public AiInsightDTO getMonthlyInsight(Long userId, String month, boolean forceRefresh) {
        if (!isAiEnabled()) {
            return AiInsightDTO.builder()
                    .month(month)
                    .summary(fallbackFactory.aiDisabled())
                    .fromCache(false)
                    .errorCode(AiErrorCode.AI_DISABLED.name())
                    .build();
        }

        FinancialSnapshotDTO snapshot = snapshotService.buildFullSnapshot(userId, month);
        String snapshotHash = SnapshotHashUtil.computeHash(snapshot);
        String knowledgeBaseHash = availabilityService.getAvailability().getKnowledgeBaseHash();

        Optional<MonthlyStatement> existingStatement = statementRepository
                .findByUserIdAndMonth(userId, month);

        if (existingStatement.isPresent()
                && isInsightCacheValid(existingStatement.get(), snapshotHash, knowledgeBaseHash)
                && (!forceRefresh || isRefreshCooldownActive(existingStatement.get()))) {
            MonthlyStatement stmt = existingStatement.get();
            log.info("AI insight cache hit for user {} month {}", userId, month);
            return AiInsightDTO.builder()
                    .month(month)
                    .summary(stmt.getCachedInsight())
                    .fromCache(true)
                    .generatedAt(stmt.getInsightCachedAt() != null ? stmt.getInsightCachedAt().toString() : null)
                    .build();
        }

        String prompt = promptBuilder.buildInsightPrompt(snapshot);
        String insight;
        try {
            insight = generate(prompt);
        } catch (Exception e) {
            log.error("Gemini monthly insight invocation failed: {}", e.getMessage(), e);
            insight = fallbackFactory.aiUnavailable();
        }

        if (!AiOutputValidator.validate(insight)) {
            log.warn("Monthly insight contains suspicious financial numbers for user {}", userId);
            auditService.record(userId, "AI_OUTPUT_REJECTED", "monthly insight rejected by validator");
            insight = retryWithCorrection(prompt, snapshot);
            if (!AiOutputValidator.validate(insight)) {
                insight = fallbackFactory.outputValidationFailed(snapshot);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        MonthlyStatement statement = existingStatement.orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return MonthlyStatement.builder()
                    .user(user)
                    .month(month)
                    .build();
        });

        statement.setCachedInsight(insight);
        statement.setSnapshotHash(snapshotHash);
        statement.setKnowledgeBaseHash(knowledgeBaseHash);
        statement.setAiModel(aiModelName);
        statement.setPromptVersion(promptVersion);
        statement.setInsightCachedAt(now);
        statement.setInsightExpiresAt(now.plusMinutes(insightTtlMinutes));
        statement.setLastAiRefreshAt(now);
        statementRepository.save(statement);

        return AiInsightDTO.builder()
                .month(month)
                .summary(insight)
                .fromCache(false)
                .generatedAt(now.toString())
                .build();
    }

    private boolean isInsightCacheValid(MonthlyStatement stmt, String snapshotHash, String knowledgeBaseHash) {
        return snapshotHash != null
                && snapshotHash.equals(stmt.getSnapshotHash())
                && safeEquals(knowledgeBaseHash, stmt.getKnowledgeBaseHash())
                && safeEquals(aiModelName, stmt.getAiModel())
                && safeEquals(promptVersion, stmt.getPromptVersion())
                && stmt.getCachedInsight() != null
                && stmt.getInsightExpiresAt() != null
                && stmt.getInsightExpiresAt().isAfter(LocalDateTime.now());
    }

    private boolean isRefreshCooldownActive(MonthlyStatement stmt) {
        return stmt.getLastAiRefreshAt() != null
                && stmt.getLastAiRefreshAt().plusSeconds(refreshCooldownSeconds).isAfter(LocalDateTime.now());
    }

    private String buildKnowledgeContext(List<KnowledgeRetrievedSegmentDTO> segments, List<AiCitationDTO> citations) {
        if (segments == null || segments.isEmpty()) {
            return "";
        }
        StringBuilder kbBuilder = new StringBuilder("\nBỐI CẢNH KIẾN THỨC BỔ TRỢ:\n");
        for (KnowledgeRetrievedSegmentDTO retrieved : segments) {
            kbBuilder.append("- ").append(resolveSegmentText(retrieved)).append("\n");
            AiCitationDTO citation = toCitation(retrieved);
            if (citation.getTitle() != null
                    && citations.stream().noneMatch(existing -> safeEquals(existing.getTitle(), citation.getTitle())
                            && safeEquals(existing.getSourceKey(), citation.getSourceKey()))) {
                citations.add(citation);
            }
        }
        return kbBuilder.toString();
    }

    private AiCitationDTO toCitation(KnowledgeRetrievedSegmentDTO retrieved) {
        return AiCitationDTO.builder()
                .title(firstNonBlank(retrieved.getTitle(), metadataValue(retrieved, "title")))
                .sourceKey(firstNonBlank(retrieved.getSourceKey(), metadataValue(retrieved, "sourceKey")))
                .sourceUrl(firstNonBlank(retrieved.getSourceUrl(), metadataValue(retrieved, "sourceUrl")))
                .score(retrieved.getScore())
                .build();
    }

    private AiErrorCode classifyAiError(Exception e) {
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (message.contains("429") || message.contains("quota") || message.contains("rate")) {
            return AiErrorCode.AI_RATE_LIMITED;
        }
        return AiErrorCode.AI_UNAVAILABLE;
    }

    private boolean safeEquals(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private String retryWithCorrection(String prompt, FinancialSnapshotDTO snapshot) {
        try {
            return generate(promptBuilder.withOutputCorrection(prompt));
        } catch (Exception e) {
            log.warn("AI correction retry failed: {}", e.getMessage());
            return fallbackFactory.outputValidationFailed(snapshot);
        }
    }

    private String generate(String prompt) {
        return aiModelClient
                .filter(AiModelClient::isAvailable)
                .orElseThrow(() -> new IllegalStateException("AI model client is not available"))
                .generate(prompt);
    }

    private String resolveSegmentText(KnowledgeRetrievedSegmentDTO retrieved) {
        if (retrieved.getText() != null && !retrieved.getText().isBlank()) {
            return retrieved.getText();
        }
        TextSegment segment = retrieved.getSegment();
        return segment != null ? segment.text() : "";
    }

    private String metadataValue(KnowledgeRetrievedSegmentDTO retrieved, String key) {
        TextSegment segment = retrieved.getSegment();
        return segment != null ? segment.metadata().getString(key) : null;
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }
}
