package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.RegisterRequest;

public interface AuthService {

  void register(RegisterRequest request);

  void verifyAccount(String email, String otpCode);
}