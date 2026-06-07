package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.*;
import com.smartfinance.smart_finance_hub.dto.response.*;

import java.util.List;

public interface SupportAdminService {

    List<SupportUserResponse> getAllUsers();
    SupportUserResponse toggleUserStatus(ToggleUserStatusRequest request, String supportAdminEmail);
    void createLockRequest(LockRequestCreateDTO request, String supportAdminEmail);
    
    List<LockRequestResponse> getAllLockRequests();
    void approveLockRequest(Long requestId, String adminEmail);
    void rejectLockRequest(Long requestId, String adminEmail);
    void deleteLockRequest(Long requestId);
    
    List<AuditLogResponse> getAuditLogsByEmail(String email);

    List<ChatRequestResponse> getAllChatRequests();
    ChatMessageResponse sendChatMessage(SendChatMessageRequest request, String senderEmail);
    void resolveChat(ResolveChatRequest request);

    List<BroadcastResponse> getAllBroadcasts();
    BroadcastResponse createBroadcast(CreateBroadcastRequest request, String supportAdminEmail);
}
