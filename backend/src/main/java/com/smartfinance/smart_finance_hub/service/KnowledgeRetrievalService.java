package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.response.KnowledgeRetrievedSegmentDTO;
import java.util.List;

public interface KnowledgeRetrievalService {

    List<KnowledgeRetrievedSegmentDTO> retrieveRelevant(String query);
}
