package com.smartfinance.smart_finance_hub.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests cho PiiRedactor — đảm bảo thông tin nhạy cảm được che đúng.
 */
class PiiRedactorTest {

    @Test
    @DisplayName("Redact email address")
    void shouldRedactEmail() {
        String input = "Gửi email cho tôi tại nguyen@gmail.com để liên hệ";
        String result = PiiRedactor.redact(input);
        assertFalse(result.contains("nguyen@gmail.com"));
        assertTrue(result.contains("[EMAIL_HIDDEN]"));
    }

    @Test
    @DisplayName("Redact Vietnamese phone number (0xx format)")
    void shouldRedactPhoneNumber0xx() {
        String input = "SĐT: 0912345678 để liên hệ";
        String result = PiiRedactor.redact(input);
        assertFalse(result.contains("0912345678"));
        assertTrue(result.contains("[PHONE_HIDDEN]"));
    }

    @Test
    @DisplayName("Redact Vietnamese phone number (+84 format)")
    void shouldRedactPhoneNumber84() {
        String input = "Liên hệ +84912345678";
        String result = PiiRedactor.redact(input);
        assertFalse(result.contains("+84912345678"));
        assertTrue(result.contains("[PHONE_HIDDEN]"));
    }

    @Test
    @DisplayName("Redact bank account number")
    void shouldRedactBankAccount() {
        String input = "STK 1900123456789 tại Vietcombank";
        String result = PiiRedactor.redact(input);
        System.out.println("Input:  [" + input + "]");
        System.out.println("Result: [" + result + "]");
        assertFalse(result.contains("1900123456789"), "Number should be redacted");
        assertTrue(result.contains("[BANK_ACCOUNT_HIDDEN]"), "Should contain HIDDEN tag");
    }

    @Test
    @DisplayName("Redact CCCD number (12 digits)")
    void shouldRedactCccd() {
        String input = "CCCD: 123456789012";
        String result = PiiRedactor.redact(input);
        assertFalse(result.contains("123456789012"));
        assertTrue(result.contains("[ID_HIDDEN]"));
    }

    @Test
    @DisplayName("Không thay đổi text không chứa PII")
    void shouldNotChangeTextWithoutPii() {
        String input = "Tháng này tôi chi 5 triệu cho ăn uống";
        String result = PiiRedactor.redact(input);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("Handle null/blank input")
    void shouldHandleNullAndBlank() {
        assertNull(PiiRedactor.redact(null));
        assertEquals("", PiiRedactor.redact(""));
        assertEquals("   ", PiiRedactor.redact("   "));
    }

    @Test
    @DisplayName("Redact multiple PII types in same string")
    void shouldRedactMultiplePiiTypes() {
        String input = "Email: test@example.com, SĐT: 0987654321, tài khoản: 1234567890123";
        String result = PiiRedactor.redact(input);
        assertFalse(result.contains("test@example.com"));
        assertFalse(result.contains("0987654321"));
        assertTrue(result.contains("[EMAIL_HIDDEN]"));
        assertTrue(result.contains("[PHONE_HIDDEN]"));
    }
}
