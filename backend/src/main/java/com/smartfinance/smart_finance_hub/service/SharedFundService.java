package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.InviteMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondInvitationRequest;
import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import com.smartfinance.smart_finance_hub.entity.Transaction;

public interface SharedFundService {

    // Task 1: Quản lý thành viên
    FundInvitation inviteMember(Long fundId, InviteMemberRequest request, Long inviterUserId);

    void respondToInvitation(Long fundId, RespondInvitationRequest request, Long respondUserId);

    // Task 2: Phê duyệt giao dịch
    Transaction createFundTransaction(Long fundId, CreateFundTransactionRequest request, Long userId);

    Transaction approveTransaction(Long transactionId, Long approverUserId);

    // Task 3: Thêm các API quản lý Quỹ nhóm hoàn chỉnh
    java.util.List<com.smartfinance.smart_finance_hub.entity.ShareFund> getFundsForUser(Long userId);

    com.smartfinance.smart_finance_hub.entity.ShareFund createFund(String name, java.math.BigDecimal target, java.math.BigDecimal initialContribution, Long userId);

    com.smartfinance.smart_finance_hub.entity.ShareFund renameFund(Long fundId, String newName, Long userId);

    void leaveFund(Long fundId, Long userId);

    void removeMember(Long fundId, String memberEmail, Long ownerUserId);

    void deleteFund(Long fundId, Long userId);

    java.util.List<com.smartfinance.smart_finance_hub.entity.Transaction> getFundTransactions(Long fundId, Long userId);

    java.util.List<java.util.Map<String, Object>> getActivitiesForUser(Long userId);

    com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse sendChatMessage(Long fundId, com.smartfinance.smart_finance_hub.dto.request.FundChatMessageRequest request, Long userId);

    java.util.List<com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse> getDiscussions(Long fundId, Long userId);

    void verifyInvitationToken(String token, Long userId);
}
