package com.smartfinance.smart_finance_hub.exception;

import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.exception.base.BaseException;
import com.smartfinance.smart_finance_hub.exception.business.UserAlreadyExistsException;
import jakarta.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        log.error("handleBaseException: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), ex.getStatus());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserExists(UserAlreadyExistsException ex) {
        log.error("handleUserExists: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        log.error("handleValidationExceptions: {}", fieldErrors);
        return new ResponseEntity<>(
                ApiResponse.error("Invalid request data", fieldErrors),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("handleIllegalArgument: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        log.error("handleIllegalState: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex) {
        log.error("handleOptimisticLock: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                ApiResponse.error("Data was updated by another request. Please retry."),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("handleTypeMismatch: {}", ex.getMessage());
        return new ResponseEntity<>(
                ApiResponse.error("Invalid parameter: " + ex.getName()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessaging(MessagingException ex) {
        log.error("handleMessaging: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                ApiResponse.error("Unable to send email. Please try again later."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiResponse<Void>> handleMail(MailException ex) {
        log.error("handleMail: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                ApiResponse.error("Không thể gửi email. Vui lòng kiểm tra cấu hình MAIL_USERNAME và MAIL_PASSWORD."),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        log.error("handleGlobalException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                ApiResponse.error("Hệ thống gặp sự cố: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
