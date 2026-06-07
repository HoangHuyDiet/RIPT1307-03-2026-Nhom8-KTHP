package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSupportChatRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String priority;
    
    private String email;
    private String name;
}
