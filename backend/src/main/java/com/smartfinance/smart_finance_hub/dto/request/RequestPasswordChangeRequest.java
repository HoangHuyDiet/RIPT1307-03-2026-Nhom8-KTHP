package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPasswordChangeRequest {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String oldPassword;
}
