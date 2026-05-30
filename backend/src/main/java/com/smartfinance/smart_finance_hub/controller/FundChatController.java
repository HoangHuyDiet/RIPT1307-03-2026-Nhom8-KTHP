package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.FundChatMessageRequest;
import com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.SharedFundService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class FundChatController {

    private final SharedFundService sharedFundService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/funds/{id}/chat.send")
    public void sendMessage(
            @DestinationVariable("id") Long fundId,
            FundChatMessageRequest request,
            Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalArgumentException("WebSocket user is not authenticated");
        }
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("WebSocket user not found"));
        FundDiscussionResponse response = sharedFundService.sendChatMessage(fundId, request, user.getId());
        messagingTemplate.convertAndSend("/topic/funds/" + fundId + "/chat", response);
    }
}


