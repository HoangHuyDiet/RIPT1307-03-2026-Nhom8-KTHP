package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveChatRequest {
    @NotNull
    private Long chatId;
}
