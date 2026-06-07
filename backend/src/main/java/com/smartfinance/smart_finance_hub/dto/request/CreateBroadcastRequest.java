package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBroadcastRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String target = "ALL";

    private String urgency = "INFO";
}
