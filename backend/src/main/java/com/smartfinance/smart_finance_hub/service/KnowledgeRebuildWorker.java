package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.entity.KnowledgeDocument;
import com.smartfinance.smart_finance_hub.enums.KnowledgeStatus;
import com.smartfinance.smart_finance_hub.enums.RebuildStatus;
import com.smartfinance.smart_finance_hub.repository.KnowledgeDocumentRepository;
import com.smartfinance.smart_finance_hub.repository.KnowledgeChunkRepository;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeRebuildWorker {
    private final Optional<GoogleAiEmbeddingModel> documentEmbeddingModel;
    private final Optional<InMemoryEmbeddingStore<TextSegment>> embeddingStore;
    private final KnowledgeDocumentRepository docRepo;
    private final KnowledgeChunkRepository chunkRepo;
    private final RagStatusService ragStatusService;
    private final KnowledgeChunkPersistenceService chunkPersistenceService;

    @Value("${ai.rag.chunk-size-chars:1200}") private int chunkSize;
    @Value("${ai.rag.chunk-overlap-chars:150}") private int chunkOverlap;

    @Async("ragRebuildExecutor")
    public void rebuildAllAsync() {
        if (embeddingStore.isEmpty() || documentEmbeddingModel.isEmpty()) {
            ragStatusService.updateStatus(RebuildStatus.FAILED, "RAG Beans are disabled.", 0);
            return;
        }

        try {
            embeddingStore.get().removeAll();
            chunkPersistenceService.clearOldChunksInTransaction();

            List<KnowledgeDocument> approvedDocs = docRepo.findByStatus(KnowledgeStatus.APPROVED);
            int totalVectorsAdded = 0;

            for (KnowledgeDocument doc : approvedDocs) {
                Metadata metadata = new Metadata();
                metadata.put("documentId", String.valueOf(doc.getId()));
                metadata.put("title", Objects.toString(doc.getTitle(), ""));
                metadata.put("category", Objects.toString(doc.getCategory(), ""));
                metadata.put("jurisdiction", Objects.toString(doc.getJurisdiction(), "GLOBAL"));
                metadata.put("sourceUrl", Objects.toString(doc.getSourceUrl(), ""));

                Document lc4jDoc = Document.from(doc.getContent(), metadata);
                DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);
                List<TextSegment> segments = splitter.split(lc4jDoc);

                List<TextSegment> enrichedSegments = new ArrayList<>();
                for (TextSegment segment : segments) {
                    String enrichedContent = String.format(
                        "[Tài liệu: %s] [Phạm vi: %s] [Nguồn: %s]\n%s",
                        doc.getTitle(), doc.getJurisdiction(),
                        Objects.toString(doc.getSourceName(), "Nội bộ"),
                        segment.text());
                    enrichedSegments.add(TextSegment.from(enrichedContent, segment.metadata()));
                }

                var response = documentEmbeddingModel.get().embedAll(enrichedSegments);
                List<Embedding> embeddings = response.content();

                for (int i = 0; i < enrichedSegments.size(); i++) {
                    TextSegment enriched = enrichedSegments.get(i);
                    Embedding embedding = embeddings.get(i);
                    String embeddingId = embeddingStore.get().add(embedding, enriched);

                    chunkPersistenceService.saveChunkInTransaction(doc, i, segments.get(i).text(), embeddingId);
                    totalVectorsAdded++;
                }
            }

            ragStatusService.updateStatus(RebuildStatus.SUCCESS, "", totalVectorsAdded);
        } catch (Exception e) {
            log.error("Rebuild Vector Store error: {}. Emergency cleaning...", e.getMessage());
            try {
                embeddingStore.get().removeAll();
                chunkPersistenceService.clearOldChunksInTransaction();
            } catch (Exception ex) {
                log.error("Emergency cleanup failed: {}", ex.getMessage());
            }
            ragStatusService.updateStatus(RebuildStatus.FAILED, e.getMessage(), 0);
        }
    }
}
