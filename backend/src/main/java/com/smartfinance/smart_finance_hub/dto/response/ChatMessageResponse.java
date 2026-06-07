package com.smartfinance.smart_finance_hub.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private Long id;
    private String sender;
    private String content;
    private LocalDateTime time;
}
