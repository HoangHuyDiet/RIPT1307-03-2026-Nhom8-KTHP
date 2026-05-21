package com.smartfinance.smart_finance_hub.exception.base;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {
  private final HttpStatus status;

  public BaseException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }
}