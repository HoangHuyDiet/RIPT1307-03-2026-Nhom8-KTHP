package com.smartfinance.smart_finance_hub.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class AiOutputValidator {

    private AiOutputValidator() {}

    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
        "(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d+)?)" +   
        "\\s*" +
        "(VND|đồng|triệu|tỷ|nghìn|ngàn|VNĐ)",       
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final Pattern PERCENT_PATTERN = Pattern.compile(
        "(-?\\d{1,3}(?:[.,]\\d+)?)\\s*%"
    );

    public static boolean validate(String aiOutput) {
        if (aiOutput == null || aiOutput.isBlank()) {
            return true;
        }

        boolean safe = true;

        Matcher currencyMatcher = CURRENCY_PATTERN.matcher(aiOutput);
        while (currencyMatcher.find()) {
            String numStr = currencyMatcher.group(1).replace(".", "").replace(",", ".");
            String unit = currencyMatcher.group(2).toLowerCase();

            try {
                double value = Double.parseDouble(numStr);
                double valueInVnd = convertToVnd(value, unit);

                if (valueInVnd > 100_000_000_000.0) {
                    log.warn("AiOutputValidator: Phát hiện số tiền rất lớn: {} {} (~{} VND)",
                        currencyMatcher.group(1), unit, String.format("%.0f", valueInVnd));
                    safe = false;
                }
            } catch (NumberFormatException e) {
            }
        }

        Matcher percentMatcher = PERCENT_PATTERN.matcher(aiOutput);
        while (percentMatcher.find()) {
            String numStr = percentMatcher.group(1).replace(",", ".");
            try {
                double value = Double.parseDouble(numStr);
                if (value > 10000.0 || value < -10000.0) {
                    log.warn("AiOutputValidator: Phát hiện tỷ lệ bất thường: {}%", value);
                    safe = false;
                }
            } catch (NumberFormatException e) {
            }
        }

        return safe;
    }

    private static double convertToVnd(double value, String unit) {
        return switch (unit) {
            case "tỷ" -> value * 1_000_000_000;
            case "triệu" -> value * 1_000_000;
            case "nghìn", "ngàn" -> value * 1_000;
            default -> value;  
        };
    }
}
