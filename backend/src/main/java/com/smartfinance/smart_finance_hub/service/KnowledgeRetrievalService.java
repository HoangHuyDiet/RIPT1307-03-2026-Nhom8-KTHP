package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.KnowledgeRetrievedSegmentDTO;
import com.smartfinance.smart_finance_hub.enums.RebuildStatus;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeRetrievalService {

    private final Optional<GoogleAiEmbeddingModel> documentEmbeddingModel;
    private final Optional<InMemoryEmbeddingStore<TextSegment>> embeddingStore;
    private final RagStatusService ragStatusService;

    @Value("${ai.rag.top-k:4}")
    private int topK;

    @Value("${ai.rag.min-score:0.70}")
    private double minScore;

    public List<KnowledgeRetrievedSegmentDTO> retrieveKnowledge(String query) {
        if (embeddingStore.isEmpty() || documentEmbeddingModel.isEmpty()) {
            return new ArrayList<>();
        }

        RebuildStatus currentStatus = ragStatusService.getCurrentStatus();
        if (currentStatus == RebuildStatus.RUNNING || currentStatus == RebuildStatus.FAILED) {
            log.warn("Bỏ qua Retrieval vì RAG đang ở trạng thái {}", currentStatus);
            return new ArrayList<>();
        }

        try {
            var queryEmbedding = documentEmbeddingModel.get().embed(query).content();
            
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(topK)
                    .minScore(minScore)
                    .build();

            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.get().search(request).matches();

            return matches.stream().map(match -> KnowledgeRetrievedSegmentDTO.builder()
                    .content(match.embedded().text())
                    .score(match.score())
                    .title(match.embedded().metadata().getString("title"))
                    .sourceUrl(match.embedded().metadata().getString("sourceUrl"))
                    .build()
            ).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Lỗi khi retrieve kiến thức: ", e);
            return new ArrayList<>();
        }
    }
}
