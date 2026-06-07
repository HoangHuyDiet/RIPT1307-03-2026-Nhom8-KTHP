package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.config.condition.RagEnabledCondition;
import com.smartfinance.smart_finance_hub.dto.response.KnowledgeRetrievedSegmentDTO;
import com.smartfinance.smart_finance_hub.service.KnowledgeRetrievalService;
import com.smartfinance.smart_finance_hub.util.PiiRedactor;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Conditional(RagEnabledCondition.class)
@RequiredArgsConstructor
@Slf4j
public class KnowledgeRetrievalServiceImpl implements KnowledgeRetrievalService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Value("${ai.rag.top-k:4}")
    private int topK;

    @Value("${ai.rag.min-score:0.70}")
    private double minScore;

    @Override
    public List<KnowledgeRetrievedSegmentDTO> retrieveRelevant(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        try {
            log.debug("RAG retrieval query: '{}'", PiiRedactor.redact(query));
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .minScore(minScore)
                .build();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

            log.debug("RAG retrieval matched {} segments with minScore={}", matches.size(), minScore);
            return matches.stream()
                .map(match -> KnowledgeRetrievedSegmentDTO.builder()
                    .segment(match.embedded())
                    .text(match.embedded().text())
                    .title(match.embedded().metadata().getString("title"))
                    .sourceKey(match.embedded().metadata().getString("sourceKey"))
                    .sourceUrl(match.embedded().metadata().getString("sourceUrl"))
                    .score(match.score())
                    .build())
                .toList();
        } catch (Exception e) {
            log.warn("RAG retrieval failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
