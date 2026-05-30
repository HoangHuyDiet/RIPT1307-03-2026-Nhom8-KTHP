package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoveMemberRequest {

    @NotNull(message = "fundId is required")
    private Long fundId;

    @NotBlank(message = "memberEmail is required")
    @Email(message = "memberEmail is invalid")
    private String memberEmail;
}


