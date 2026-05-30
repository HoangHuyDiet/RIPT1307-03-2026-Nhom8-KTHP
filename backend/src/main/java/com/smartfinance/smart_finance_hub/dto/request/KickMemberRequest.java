package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KickMemberRequest {

    @NotBlank(message = "Member email is required")
    @Email(message = "Invalid email")
    private String memberEmail;

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must be at most 500 characters")
    private String reason;
}


