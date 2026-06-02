package com.smartfinance.smart_finance_hub.exception.business;

import com.smartfinance.smart_finance_hub.exception.base.BaseException;
import org.springframework.http.HttpStatus;

public class EmailDeliveryException extends BaseException {
  public EmailDeliveryException(String message) {
    super(message, HttpStatus.SERVICE_UNAVAILABLE);
  }
}
