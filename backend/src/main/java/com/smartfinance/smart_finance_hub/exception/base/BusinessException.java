package com.smartfinance.smart_finance_hub.exception.base;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, HttpStatus status) {
        super(message, status);
    }
}