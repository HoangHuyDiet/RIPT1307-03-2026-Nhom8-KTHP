package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.CreateSupportChatRequest;
import com.smartfinance.smart_finance_hub.dto.request.SendChatMessageRequest;
import com.smartfinance.smart_finance_hub.dto.response.ChatMessageResponse;
import com.smartfinance.smart_finance_hub.dto.response.ChatRequestResponse;

import java.util.List;

public interface UserSupportService {

    List<ChatRequestResponse> getUserChatRequests(String email);

    ChatRequestResponse createChatRequest(CreateSupportChatRequest request, String email);

    ChatMessageResponse sendChatMessage(SendChatMessageRequest request, String email);
}
