package com.smartfinance.smart_finance_hub.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kiểm tra output AI có chứa số tài chính bất thường — context-aware.
 * 
 * Logic:
 * - Chỉ validate các số lớn (>= 1,000,000 VND hoặc >= 10% tỷ lệ phần trăm)
 * - Bỏ qua số nhỏ (index, thứ tự, số tháng, v.v.)
 * - Phân biệt tiền VND (12.500.000, 12,5 triệu) vs phần trăm (12,5%)
 * - Trả về true nếu nội dung an toàn, false nếu phát hiện bất thường
 */
@Slf4j
public final class AiOutputValidator {

    private AiOutputValidator() {}

    // Regex: số kèm đơn vị VND / đồng / triệu / tỷ
    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
        "(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d+)?)" +   // số
        "\\s*" +
        "(VND|đồng|triệu|tỷ|nghìn|ngàn|VNĐ)",       // đơn vị
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    // Regex: phần trăm (bao gồm số âm)
    private static final Pattern PERCENT_PATTERN = Pattern.compile(
        "(-?\\d{1,3}(?:[.,]\\d+)?)\\s*%"
    );

    /**
     * Validate output AI: trả true nếu nội dung an toàn.
     * Hiện tại chỉ log warning — caller quyết định có chặn không.
     */
    public static boolean validate(String aiOutput) {
        if (aiOutput == null || aiOutput.isBlank()) {
            return true;
        }

        boolean safe = true;

        // Kiểm tra số tiền
        Matcher currencyMatcher = CURRENCY_PATTERN.matcher(aiOutput);
        while (currencyMatcher.find()) {
            String numStr = currencyMatcher.group(1).replace(".", "").replace(",", ".");
            String unit = currencyMatcher.group(2).toLowerCase();

            try {
                double value = Double.parseDouble(numStr);
                double valueInVnd = convertToVnd(value, unit);

                // Flag số tiền > 100 tỷ VND — có thể là hallucination
                if (valueInVnd > 100_000_000_000.0) {
                    log.warn("AiOutputValidator: Phát hiện số tiền rất lớn: {} {} (~{} VND)",
                        currencyMatcher.group(1), unit, String.format("%.0f", valueInVnd));
                    safe = false;
                }
            } catch (NumberFormatException e) {
                // Bỏ qua lỗi parse
            }
        }

        // Kiểm tra phần trăm
        Matcher percentMatcher = PERCENT_PATTERN.matcher(aiOutput);
        while (percentMatcher.find()) {
            String numStr = percentMatcher.group(1).replace(",", ".");
            try {
                double value = Double.parseDouble(numStr);
                // Flag phần trăm > 100% hoặc < 0%
                if (value > 100.0 || value < 0.0) {
                    log.warn("AiOutputValidator: Phát hiện tỷ lệ bất thường: {}%", value);
                    safe = false;
                }
            } catch (NumberFormatException e) {
                // Bỏ qua
            }
        }

        return safe;
    }

    private static double convertToVnd(double value, String unit) {
        return switch (unit) {
            case "tỷ" -> value * 1_000_000_000;
            case "triệu" -> value * 1_000_000;
            case "nghìn", "ngàn" -> value * 1_000;
            default -> value;  // VND, đồng, VNĐ
        };
    }
}
