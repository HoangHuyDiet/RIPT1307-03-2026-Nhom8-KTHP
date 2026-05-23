package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespondInvitationRequest {

    @NotNull(message = "ID lời mời không được để trống")
    private Long invitationId;

    @NotBlank(message = "Hành động phản hồi không được để trống (ACCEPT/REJECT)")
    private String action; // "ACCEPT" hoặc "REJECT"
}
