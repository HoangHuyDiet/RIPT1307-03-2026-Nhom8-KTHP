package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class SendChatMessageRequest {
    @NotNull
    private Long chatId;

    @NotNull
    private Map<String, Object> message;
}
