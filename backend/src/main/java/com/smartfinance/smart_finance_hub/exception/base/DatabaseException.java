package com.smartfinance.smart_finance_hub.exception.base;

import org.springframework.http.HttpStatus;

public class DatabaseException extends BaseException {
    public DatabaseException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}