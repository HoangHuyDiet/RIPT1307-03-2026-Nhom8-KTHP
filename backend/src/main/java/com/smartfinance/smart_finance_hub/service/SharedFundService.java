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
}
