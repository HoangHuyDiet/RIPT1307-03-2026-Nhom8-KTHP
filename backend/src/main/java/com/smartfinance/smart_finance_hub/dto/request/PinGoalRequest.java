package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PinGoalRequest {
    @NotNull(message = "isPinned is required")
    private Boolean isPinned;
}
