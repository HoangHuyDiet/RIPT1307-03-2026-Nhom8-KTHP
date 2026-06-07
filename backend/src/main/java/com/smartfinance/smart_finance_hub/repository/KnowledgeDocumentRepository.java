package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.KnowledgeDocument;
import com.smartfinance.smart_finance_hub.enums.KnowledgeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    Optional<KnowledgeDocument> findBySourceKey(String sourceKey);

    List<KnowledgeDocument> findByStatus(KnowledgeStatus status);

    long countByStatus(KnowledgeStatus status);
}
