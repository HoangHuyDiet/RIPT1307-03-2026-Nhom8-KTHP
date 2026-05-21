package com.smartfinance.smart_finance_hub.exception.business;

import org.springframework.http.HttpStatus;
import com.smartfinance.smart_finance_hub.exception.base.BusinessException;

public class UserAlreadyExistsException extends BusinessException{

  public UserAlreadyExistsException(String message) {
    super(message, HttpStatus.CONFLICT);
  }
}