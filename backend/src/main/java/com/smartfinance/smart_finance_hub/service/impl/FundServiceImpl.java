package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.ApproveFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.CreateFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.DisbandFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.FundChatMessageRequest;
import com.smartfinance.smart_finance_hub.dto.request.FundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.InviteMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.KickMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondByTokenRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondInvitationRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateFundRequest;
import com.smartfinance.smart_finance_hub.dto.response.BudgetChartResponse;
import com.smartfinance.smart_finance_hub.dto.response.DisbandStatusResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundActivityResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundListResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundStatResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundTransactionResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundInvitationResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundMemberResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundStatisticsResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundTransactionResponse;
import com.smartfinance.smart_finance_hub.dto.response.TopContributorResponse;
import com.smartfinance.smart_finance_hub.service.FundService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundChatApplicationService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundCoreApplicationService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundMemberApplicationService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundTransactionApplicationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundServiceImpl implements FundService {

    private final FundCoreApplicationService coreService;
    private final FundMemberApplicationService memberService;
    private final FundTransactionApplicationService transactionService;
    private final FundChatApplicationService chatService;

    @Override
    public FundResponse createFund(CreateFundRequest request, Long userId) {
        return coreService.createFund(request, userId);
    }

    @Override
    public FundResponse getFundById(Long fundId, Long userId) {
        return coreService.getFundById(fundId, userId);
    }

    @Override
    public List<FundResponse> getMyFunds(Long userId) {
        return coreService.getMyFunds(userId);
    }

    @Override
    public List<FeFundListResponse> getFrontendFundList(Long userId) {
        return coreService.getFrontendFundList(userId);
    }

    @Override
    public List<FeFundStatResponse> getFrontendFundStats(Long userId) {
        return coreService.getFrontendFundStats(userId);
    }

    @Override
    public List<FeFundActivityResponse> getFrontendFundActivities(Long userId) {
        return coreService.getFrontendFundActivities(userId);
    }

    @Override
    public FundResponse updateFund(Long fundId, UpdateFundRequest request, Long userId) {
        return coreService.updateFund(fundId, request, userId);
    }

    @Override
    public FundResponse renameFund(Long fundId, String newName, Long userId) {
        return coreService.renameFund(fundId, newName, userId);
    }

    @Override
    public List<FundMemberResponse> getMembers(Long fundId, Long userId) {
        return memberService.getMembers(fundId, userId);
    }

    @Override
    public FundInvitationResponse inviteMember(Long fundId, InviteMemberRequest request, Long inviterUserId) {
        return memberService.inviteMember(fundId, request, inviterUserId);
    }

    @Override
    public void respondToInvitation(Long fundId, RespondInvitationRequest request, Long respondUserId) {
        memberService.respondToInvitation(fundId, request, respondUserId);
    }

    @Override
    public void respondToInvitationByToken(String token, RespondByTokenRequest request, Long respondUserId) {
        memberService.respondToInvitationByToken(token, request, respondUserId);
    }

    @Override
    public FundInvitationResponse kickMember(Long fundId, KickMemberRequest request, Long ownerUserId) {
        return memberService.kickMember(fundId, request, ownerUserId);
    }

    @Override
    public FundInvitationResponse requestRemoveMember(Long fundId, String memberEmail, Long ownerUserId) {
        return memberService.requestRemoveMember(fundId, memberEmail, ownerUserId);
    }

    @Override
    public void leaveFund(Long fundId, Long userId) {
        memberService.leaveFund(fundId, userId);
    }

    @Override
    public DisbandStatusResponse proposeDisbandFund(
            Long fundId, DisbandFundRequest request, Long ownerUserId) {
        return memberService.proposeDisbandFund(fundId, request, ownerUserId);
    }

    @Override
    public DisbandStatusResponse getDisbandStatus(Long fundId, Long userId) {
        return memberService.getDisbandStatus(fundId, userId);
    }

    @Override
    public List<FundInvitationResponse> getMyPendingInvitations(Long userId) {
        return memberService.getMyPendingInvitations(userId);
    }

    @Override
    public List<FundInvitationResponse> getFundInvitations(Long fundId, Long userId) {
        return memberService.getFundInvitations(fundId, userId);
    }

    @Override
    public FundTransactionResponse createFundTransaction(
            Long fundId, CreateFundTransactionRequest request, Long userId) {
        return transactionService.createFundTransaction(fundId, request, userId);
    }

    @Override
    public FeFundTransactionResponse createFrontendTransactionRequest(
            FundTransactionRequest request, Long userId) {
        return transactionService.createFrontendTransactionRequest(request, userId);
    }

    @Override
    public FundTransactionResponse approveTransaction(Long transactionId, Long approverUserId) {
        return transactionService.approveTransaction(transactionId, approverUserId);
    }

    @Override
    public FeFundTransactionResponse approveOrRejectFrontendTransaction(
            ApproveFundTransactionRequest request, Long approverUserId) {
        return transactionService.approveOrRejectFrontendTransaction(request, approverUserId);
    }

    @Override
    public Page<FundTransactionResponse> getFundTransactions(
            Long fundId, Long userId, String type, Pageable pageable) {
        return transactionService.getFundTransactions(fundId, userId, type, pageable);
    }

    @Override
    public List<FeFundTransactionResponse> getFrontendFundTransactions(Long fundId, Long userId) {
        return transactionService.getFrontendFundTransactions(fundId, userId);
    }

    @Override
    public List<TopContributorResponse> getTopContributors(Long fundId, Long userId) {
        return transactionService.getTopContributors(fundId, userId);
    }

    @Override
    public List<BudgetChartResponse> getBudgetChart(Long fundId, Long userId) {
        return transactionService.getBudgetChart(fundId, userId);
    }

    @Override
    public List<FundDiscussionResponse> getDiscussions(Long fundId, Long userId) {
        return chatService.getDiscussions(fundId, userId);
    }

    @Override
    public FundDiscussionResponse sendChatMessage(Long fundId, FundChatMessageRequest request, Long userId) {
        return chatService.sendChatMessage(fundId, request, userId);
    }

    @Override
    public FundStatisticsResponse getFundStatistics(Long fundId, Long userId) {
        return transactionService.getFundStatistics(fundId, userId);
    }
}
