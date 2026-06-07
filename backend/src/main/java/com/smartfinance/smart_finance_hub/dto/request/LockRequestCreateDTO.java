package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LockRequestCreateDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String reason;
}
