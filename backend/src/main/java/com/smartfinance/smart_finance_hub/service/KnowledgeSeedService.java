package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.entity.KnowledgeDocument;
import com.smartfinance.smart_finance_hub.enums.KnowledgeStatus;
import com.smartfinance.smart_finance_hub.repository.KnowledgeDocumentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeSeedService {

    private final KnowledgeDocumentRepository docRepo;
    private final KnowledgeIngestionService ingestionService;

    @PostConstruct
    public void seedData() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:knowledge/*.yml");

            boolean hasChanges = false;

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                String content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                
                KnowledgeDocument doc = docRepo.findBySourceUrl(filename).orElse(new KnowledgeDocument());
                
                doc.setTitle(filename.replace(".yml", "").replace("-", " ").toUpperCase());
                doc.setSourceUrl(filename);
                doc.setSourceName("Hệ Thống (System Seed)");
                doc.setCategory("KIẾN THỨC CHUNG");
                doc.setJurisdiction("VIỆT NAM");
                doc.setStatus(KnowledgeStatus.APPROVED);
                
                if (doc.getId() == null || !content.equals(doc.getContent())) {
                    doc.setContent(content);
                    hasChanges = true;
                }
                
                docRepo.save(doc);
            }

            if (hasChanges) {
                log.info("Phát hiện thay đổi trong dữ liệu mẫu, tiến hành yêu cầu Rebuild RAG...");
                ingestionService.requestRebuild();
            } else {
                log.info("Dữ liệu RAG mẫu không có thay đổi mới.");
            }

        } catch (Exception e) {
            log.error("Lỗi khi seed dữ liệu RAG: ", e);
        }
    }
}
