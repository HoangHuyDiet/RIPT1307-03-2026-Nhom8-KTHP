package com.smartfinance.smart_finance_hub.exception;

import com.smartfinance.smart_finance_hub.enums.AiErrorCode;
import lombok.Getter;

@Getter
public class AiException extends RuntimeException {
    private final AiErrorCode errorCode;

    public AiException(AiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
