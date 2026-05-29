package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.CreateFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.DisbandFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.ApproveFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.FundChatMessageRequest;
import com.smartfinance.smart_finance_hub.dto.request.FundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.InternalTransferRequest;
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
import com.smartfinance.smart_finance_hub.dto.response.FundNotificationResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundStatisticsResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundTransactionResponse;
import com.smartfinance.smart_finance_hub.dto.response.PersonalFundDashboardResponse;
import com.smartfinance.smart_finance_hub.dto.response.TopContributorResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FundService {

    FundResponse createFund(CreateFundRequest request, Long userId);

    FundResponse getFundById(Long fundId, Long userId);

    List<FundResponse> getMyFunds(Long userId);

    List<FundResponse> getMyFunds(Long userId, String fundTypeFilter);

    List<FeFundListResponse> getFrontendFundList(Long userId);

    List<FeFundListResponse> getFrontendFundList(Long userId, String fundTypeFilter);

    List<FeFundStatResponse> getFrontendFundStats(Long userId);

    List<FeFundActivityResponse> getFrontendFundActivities(Long userId);

    FundResponse updateFund(Long fundId, UpdateFundRequest request, Long userId);

    FundResponse renameFund(Long fundId, String newName, Long userId);

    FundResponse closeFund(Long fundId, Long userId);

    PersonalFundDashboardResponse getPersonalDashboard(Long userId);

    List<FundMemberResponse> getMembers(Long fundId, Long userId);

    FundInvitationResponse inviteMember(Long fundId, InviteMemberRequest request, Long inviterUserId);

    void respondToInvitation(Long fundId, RespondInvitationRequest request, Long respondUserId);

    void respondToInvitationByToken(String token, RespondByTokenRequest request, Long respondUserId);

    FundInvitationResponse kickMember(Long fundId, KickMemberRequest request, Long ownerUserId);

    FundInvitationResponse requestRemoveMember(Long fundId, String memberEmail, Long ownerUserId);

    void leaveFund(Long fundId, Long userId);

    DisbandStatusResponse proposeDisbandFund(Long fundId, DisbandFundRequest request, Long ownerUserId);

    DisbandStatusResponse getDisbandStatus(Long fundId, Long userId);

    List<FundInvitationResponse> getMyPendingInvitations(Long userId);

    List<FundInvitationResponse> getFundInvitations(Long fundId, Long userId);

    FundTransactionResponse createFundTransaction(Long fundId, CreateFundTransactionRequest request, Long userId);

    FeFundTransactionResponse createFrontendTransactionRequest(FundTransactionRequest request, Long userId);

    FundTransactionResponse approveTransaction(Long transactionId, Long approverUserId);

    FeFundTransactionResponse approveOrRejectFrontendTransaction(
            ApproveFundTransactionRequest request, Long approverUserId);

    Page<FundTransactionResponse> getFundTransactions(
            Long fundId, Long userId, String type, Pageable pageable);

    List<FeFundTransactionResponse> getFrontendFundTransactions(Long fundId, Long userId);

    List<FeFundTransactionResponse> getPendingTransactionRequests(Long fundId, Long userId);

    List<TopContributorResponse> getTopContributors(Long fundId, Long userId);

    List<BudgetChartResponse> getBudgetChart(Long fundId, Long userId);

    List<FundDiscussionResponse> getDiscussions(Long fundId, Long userId);

    FundDiscussionResponse sendChatMessage(Long fundId, FundChatMessageRequest request, Long userId);

    FundStatisticsResponse getFundStatistics(Long fundId, Long userId);

    void internalTransfer(InternalTransferRequest request, Long userId);
}
