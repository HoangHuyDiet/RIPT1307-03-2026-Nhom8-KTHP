package com.smartfinance.smart_finance_hub.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Redact PII before sending to LLM.
 * Protects: Email, Phone, Bank account number, National ID.
 */
@Slf4j
public final class PiiRedactor {

    private PiiRedactor() {}

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(?:\\+?84|0)\\d{9,10}"
    );

    // Bank: STK/TK + space/colon + 8-19 digits
    private static final Pattern BANK_ACCOUNT_PATTERN;
    static {
        // Build pattern programmatically to avoid encoding issues
        String keywords = "STK|TK|" +
            "t\u00e0i kho\u1ea3n|" +    // tài khoản
            "s\u1ed1 TK|" +              // số TK
            "s\u1ed1 t\u00e0i kho\u1ea3n"; // số tài khoản
        BANK_ACCOUNT_PATTERN = Pattern.compile(
            "(?:" + keywords + ")\\s*:?\\s*\\d{8,19}",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
        );
    }

    // CMND (9 digits) / CCCD (12 digits)
    private static final Pattern ID_CARD_PATTERN;
    static {
        String keywords = "CMND|CCCD|" +
            "c\u0103n c\u01b0\u1edbc|" +  // căn cước
            "ch\u1ee9ng minh";             // chứng minh
        ID_CARD_PATTERN = Pattern.compile(
            "(?:" + keywords + ")\\s*:?\\s*\\d{9}(?:\\d{3})?",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
        );
    }

    public static String redact(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String result = input;

        result = EMAIL_PATTERN.matcher(result).replaceAll("[EMAIL_HIDDEN]");
        result = BANK_ACCOUNT_PATTERN.matcher(result).replaceAll("[BANK_ACCOUNT_HIDDEN]");
        result = ID_CARD_PATTERN.matcher(result).replaceAll("[ID_HIDDEN]");
        result = PHONE_PATTERN.matcher(result).replaceAll("[PHONE_HIDDEN]");

        if (!result.equals(input)) {
            log.info("PiiRedactor: PII redacted from input");
        }

        return result;
    }
}
