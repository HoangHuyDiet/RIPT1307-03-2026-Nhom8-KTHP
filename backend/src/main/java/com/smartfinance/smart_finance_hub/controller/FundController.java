package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.CreateFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.DisbandFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.ApproveFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.FundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.InviteMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.KickMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.RemoveMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.RenameFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondByTokenRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondInvitationRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateFundRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
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
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.FundService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class FundController {

    private final FundService fundService;

    @PostMapping
    public ResponseEntity<ApiResponse<FundResponse>> createFund(
            @Valid @RequestBody CreateFundRequest request) {
        Long userId = getCurrentUserId();
        FundResponse data = fundService.createFund(request, userId);
        return new ResponseEntity<>(ApiResponse.success("Fund created successfully", data), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<FundResponse>>> getMyFunds() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getMyFunds(userId)));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<FeFundListResponse>>> getFrontendFundList() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getFrontendFundList(userId)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<List<FeFundStatResponse>>> getFrontendStats() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getFrontendFundStats(userId)));
    }

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<List<FeFundActivityResponse>>> getFrontendActivities() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getFrontendFundActivities(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FundResponse>> getFundById(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getFundById(id, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FundResponse>> updateFund(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateFundRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Fund updated successfully", fundService.updateFund(id, request, userId)));
    }

    @PutMapping("/rename")
    public ResponseEntity<ApiResponse<FundResponse>> renameFund(
            @Valid @RequestBody RenameFundRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Fund renamed successfully",
                fundService.renameFund(request.getFundId(), request.getNewName(), userId)));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<FundMemberResponse>>> getMembers(
            @PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getMembers(id, userId)));
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ApiResponse<FundInvitationResponse>> inviteMember(
            @PathVariable("id") Long id,
            @Valid @RequestBody InviteMemberRequest request) {
        Long userId = getCurrentUserId();
        return new ResponseEntity<>(
                ApiResponse.success("Invitation sent successfully",
                        fundService.inviteMember(id, request, userId)),
                HttpStatus.CREATED);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<ApiResponse<Void>> respondToInvitation(
            @PathVariable("id") Long id,
            @Valid @RequestBody RespondInvitationRequest request) {
        Long userId = getCurrentUserId();
        fundService.respondToInvitation(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Response processed successfully"));
    }

    @PostMapping("/invitations/{token}/respond")
    public ResponseEntity<ApiResponse<Void>> respondByToken(
            @PathVariable("token") String token,
            @Valid @RequestBody RespondByTokenRequest request) {
        Long userId = getCurrentUserId();
        fundService.respondToInvitationByToken(token, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Response processed successfully"));
    }

    @PostMapping("/{id}/kick")
    public ResponseEntity<ApiResponse<FundInvitationResponse>> kickMember(
            @PathVariable("id") Long id,
            @Valid @RequestBody KickMemberRequest request) {
        Long userId = getCurrentUserId();
        return new ResponseEntity<>(
                ApiResponse.success("Kick proposal sent successfully",
                        fundService.kickMember(id, request, userId)),
                HttpStatus.CREATED);
    }

    @PostMapping("/remove-request")
    public ResponseEntity<ApiResponse<FundInvitationResponse>> removeRequest(
            @Valid @RequestBody RemoveMemberRequest request) {
        Long userId = getCurrentUserId();
        return new ResponseEntity<>(
                ApiResponse.success("Remove request sent successfully",
                        fundService.requestRemoveMember(
                                request.getFundId(), request.getMemberEmail(), userId)),
                HttpStatus.CREATED);
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveFund(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        fundService.leaveFund(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Left fund successfully"));
    }

    @PostMapping("/{id}/disband")
    public ResponseEntity<ApiResponse<DisbandStatusResponse>> proposeDisbandFund(
            @PathVariable("id") Long id,
            @Valid @RequestBody DisbandFundRequest request) {
        Long userId = getCurrentUserId();
        return new ResponseEntity<>(
                ApiResponse.success("Disband proposal sent successfully",
                        fundService.proposeDisbandFund(id, request, userId)),
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}/disband/status")
    public ResponseEntity<ApiResponse<DisbandStatusResponse>> getDisbandStatus(
            @PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getDisbandStatus(id, userId)));
    }

    @GetMapping("/invitations/pending")
    public ResponseEntity<ApiResponse<List<FundInvitationResponse>>> getMyPendingInvitations() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getMyPendingInvitations(userId)));
    }

    @GetMapping("/{id}/invitations")
    public ResponseEntity<ApiResponse<List<FundInvitationResponse>>> getFundInvitations(
            @PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getFundInvitations(id, userId)));
    }

    @PostMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<FundTransactionResponse>> createFundTransaction(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateFundTransactionRequest request) {
        Long userId = getCurrentUserId();
        return new ResponseEntity<>(
                ApiResponse.success("Fund transaction created and waiting for approval",
                        fundService.createFundTransaction(id, request, userId)),
                HttpStatus.CREATED);
    }

    @PostMapping("/transaction-request")
    public ResponseEntity<ApiResponse<FeFundTransactionResponse>> createTransactionRequest(
            @Valid @RequestBody FundTransactionRequest request) {
        Long userId = getCurrentUserId();
        return new ResponseEntity<>(
                ApiResponse.success("Transaction request created",
                        fundService.createFrontendTransactionRequest(request, userId)),
                HttpStatus.CREATED);
    }

    @PostMapping("/approve-transaction")
    public ResponseEntity<ApiResponse<FeFundTransactionResponse>> approveTransactionRequest(
            @Valid @RequestBody ApproveFundTransactionRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Transaction request processed",
                fundService.approveOrRejectFrontendTransaction(request, userId)));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<FeFundTransactionResponse>>> getFrontendTransactions(
            @RequestParam("fundId") Long fundId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK",
                fundService.getFrontendFundTransactions(fundId, userId)));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<Page<FundTransactionResponse>>> getFundTransactions(
            @PathVariable("id") Long id,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by("date").descending().and(Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(
                "OK", fundService.getFundTransactions(id, userId, type, pageable)));
    }

    @PutMapping("/transactions/{id}/approve")
    public ResponseEntity<ApiResponse<FundTransactionResponse>> approveTransaction(
            @PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction approved successfully", fundService.approveTransaction(id, userId)));
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<ApiResponse<FundStatisticsResponse>> getFundStatistics(
            @PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getFundStatistics(id, userId)));
    }

    @GetMapping("/{id}/top-contributors")
    public ResponseEntity<ApiResponse<List<TopContributorResponse>>> getTopContributors(
            @PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getTopContributors(id, userId)));
    }

    @GetMapping("/{id}/budget-chart")
    public ResponseEntity<ApiResponse<List<BudgetChartResponse>>> getBudgetChart(
            @PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getBudgetChart(id, userId)));
    }

    @GetMapping("/{id}/discussions")
    public ResponseEntity<ApiResponse<List<FundDiscussionResponse>>> getDiscussions(
            @PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", fundService.getDiscussions(id, userId)));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Cannot authenticate current user");
    }
}


