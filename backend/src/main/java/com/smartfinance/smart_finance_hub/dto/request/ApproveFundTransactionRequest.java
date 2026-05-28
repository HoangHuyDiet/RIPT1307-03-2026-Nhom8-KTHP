package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveFundTransactionRequest {

    @NotNull(message = "requestId is required")
    private Long requestId;

    @NotBlank(message = "action is required")
    private String action;
}


