package com.smartfinance.smart_finance_hub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  @NotBlank(message= "Email không được để trống")
  @Email(message="Email không đúng định dạng")
  private String email;

  @NotBlank(message= "Password không được để trống")
  @Size(min=6, message= "Password phải có ít nhất 6 ký tự")
  private String password;

  @NotBlank(message= "DisplayName không được để trống")
  @Size(min=3, max=20, message= "DisplayName phải có từ 3-20 ký tự")
  private String displayName;
}