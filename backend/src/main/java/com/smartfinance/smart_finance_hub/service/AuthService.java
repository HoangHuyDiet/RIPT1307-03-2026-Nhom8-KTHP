package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.LoginRequest;
import com.smartfinance.smart_finance_hub.dto.LoginResponse;
import com.smartfinance.smart_finance_hub.dto.RegisterRequest;
import com.smartfinance.smart_finance_hub.dto.request.ChangePasswordRequest;
import com.smartfinance.smart_finance_hub.dto.request.ForgotPasswordRequest;
import com.smartfinance.smart_finance_hub.dto.request.RequestPasswordChangeRequest;

public interface AuthService {

  void register(RegisterRequest request);

  void verifyAccount(String email, String otpCode);

  void resendOtp(String email);

  LoginResponse login(LoginRequest request);

  void forgotPassword(ForgotPasswordRequest request);

  void resetPasswordWithOtp(String email, String otpCode, String newPassword);

  void requestPasswordChange(String email, RequestPasswordChangeRequest request);

  void changePassword(String email, ChangePasswordRequest request);
}
