package com.smartfinance.smart_finance_hub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyRequest {
  @NotBlank(message = "Email không được để trống")
  @Email(message = "Email không đúng định dạng")
  private String email;

  @NotBlank(message = "Mã OTP không được để trống")
  private String otpCode;
}
