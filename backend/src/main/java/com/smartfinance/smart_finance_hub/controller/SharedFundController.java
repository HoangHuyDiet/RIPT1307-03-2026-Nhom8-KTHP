package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.InviteMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondInvitationRequest;
import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.SharedFundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class SharedFundController {

    private final SharedFundService sharedFundService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return userDetails.getId();
        }
        throw new IllegalStateException("Không thể xác thực người dùng hiện tại!");
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<Map<String, Object>> inviteMember(
            @PathVariable("id") Long fundId,
            @Valid @RequestBody InviteMemberRequest request) {

        log.info("inviteMember param: fundId={}, email={}", fundId, request.getEmail());

        Long currentUserId = getCurrentUserId();
        FundInvitation invitation = sharedFundService.inviteMember(fundId, request, currentUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("invitationId", invitation.getId());
        data.put("invitedEmail", invitation.getInvitedEmail());
        data.put("status", invitation.getStatus());
        data.put("expiresAt", invitation.getExpiresAt().toString());

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "Đã gửi lời mời thành công đến " + request.getEmail());
        response.put("data", data);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<Map<String, Object>> respondToInvitation(
            @PathVariable("id") Long fundId,
            @Valid @RequestBody RespondInvitationRequest request) {

        log.info("respondToInvitation param: fundId={}, invitationId={}, action={}", fundId, request.getInvitationId(), request.getAction());

        Long currentUserId = getCurrentUserId();
        sharedFundService.respondToInvitation(fundId, request, currentUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Đã xử lý phản hồi lời mời: " + request.getAction().toUpperCase());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/transactions")
    public ResponseEntity<Map<String, Object>> createFundTransaction(
            @PathVariable("id") Long fundId,
            @Valid @RequestBody CreateFundTransactionRequest request) {

        log.info("createFundTransaction param: fundId={}", fundId);

        Long currentUserId = getCurrentUserId();
        Transaction transaction = sharedFundService.createFundTransaction(fundId, request, currentUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transaction.getId());
        data.put("amount", transaction.getAmount());
        data.put("type", transaction.getType());
        data.put("isApproved", transaction.getIsApproved());

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "Đã tạo yêu cầu giao dịch, chờ phê duyệt");
        response.put("data", data);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/transactions/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveTransaction(
            @PathVariable("id") Long transactionId) {

        log.info("approveTransaction param: transactionId={}", transactionId);

        Long currentUserId = getCurrentUserId();
        Transaction transaction = sharedFundService.approveTransaction(transactionId, currentUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transaction.getId());
        data.put("isApproved", transaction.getIsApproved());
        data.put("fundBalance", transaction.getShareFund().getBalance());

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Đã phê duyệt giao dịch thành công!");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
