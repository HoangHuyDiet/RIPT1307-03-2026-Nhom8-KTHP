package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ToggleUserStatusRequest {
    @NotBlank
    @Email
    private String email;

    @NotNull
    private Boolean checked;
}
