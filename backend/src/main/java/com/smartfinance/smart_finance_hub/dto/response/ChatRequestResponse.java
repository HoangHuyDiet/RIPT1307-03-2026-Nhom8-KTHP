package com.smartfinance.smart_finance_hub.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChatRequestResponse {
    private Long id;
    private String name;
    private String email;
    private String lastMessage;
    private LocalDateTime time;
    private String status;
    private String priority;
    private List<ChatMessageResponse> messages;
}
