package com.smartfinance.smart_finance_hub.service.impl.fund;

import com.smartfinance.smart_finance_hub.dto.request.FundChatMessageRequest;
import com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse;
import com.smartfinance.smart_finance_hub.entity.Fund;
import com.smartfinance.smart_finance_hub.entity.FundMessage;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.repository.FundMessageRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundChatApplicationService {

    private final FundMessageRepository fundMessageRepository;
    private final FundAccessService access;
    private final FundMapper mapper;

    @Transactional(readOnly = true)
    public List<FundDiscussionResponse> getDiscussions(Long fundId, Long userId) {
        access.requireFund(fundId);
        access.requireMember(fundId, userId);
        return fundMessageRepository.findByFundIdOrderByCreatedAtAsc(fundId).stream()
                .map(mapper::toDiscussionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FundDiscussionResponse sendChatMessage(Long fundId, FundChatMessageRequest request, Long userId) {
        Fund fund = access.requireActiveFund(fundId);
        access.requireMember(fundId, userId);
        User user = access.requireUser(userId);
        FundMessage message = FundMessage.builder()
                .fund(fund)
                .sender(user)
                .type("user")
                .text(access.normalizeRequired(request.getContent(), "content is required"))
                .build();
        return mapper.toDiscussionResponse(fundMessageRepository.save(message));
    }
}
