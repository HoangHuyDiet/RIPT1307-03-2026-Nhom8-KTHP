package com.smartfinance.smart_finance_hub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  private String token;
  private String refreshToken;
  private String email;
  private String displayName;
  private List<String> roles;
}

