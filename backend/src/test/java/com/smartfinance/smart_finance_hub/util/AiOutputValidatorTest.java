package com.smartfinance.smart_finance_hub.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests cho AiOutputValidator — kiểm tra phát hiện hallucination tài chính.
 */
class AiOutputValidatorTest {

    @Test
    @DisplayName("Text không có số liệu tài chính → safe")
    void shouldPassTextWithoutNumbers() {
        assertTrue(AiOutputValidator.validate("Bạn nên tiết kiệm nhiều hơn."));
    }

    @Test
    @DisplayName("Số tiền hợp lệ → safe")
    void shouldPassValidCurrencyAmount() {
        assertTrue(AiOutputValidator.validate("Chi tiêu tháng này là 5.000.000 VND"));
    }

    @Test
    @DisplayName("Số tiền rất lớn (>100 tỷ) → unsafe hallucination")
    void shouldDetectExtremelyLargeAmount() {
        assertFalse(AiOutputValidator.validate("Bạn đã chi 500 tỷ VND cho ăn uống"));
    }

    @Test
    @DisplayName("Phần trăm hợp lệ → safe")
    void shouldPassValidPercentage() {
        assertTrue(AiOutputValidator.validate("Chi tiêu chiếm 45,5% thu nhập"));
    }

    @Test
    @DisplayName("Phần trăm > 100% → unsafe")
    void shouldDetectInvalidPercentageOver100() {
        assertFalse(AiOutputValidator.validate("Chi tiêu chiếm 150% thu nhập"));
    }

    @Test
    @DisplayName("Phần trăm < 0% → unsafe")
    void shouldDetectNegativePercentage() {
        assertFalse(AiOutputValidator.validate("Tỷ lệ tiết kiệm -5%"));
    }

    @Test
    @DisplayName("Null/blank input → safe")
    void shouldPassNullAndBlank() {
        assertTrue(AiOutputValidator.validate(null));
        assertTrue(AiOutputValidator.validate(""));
        assertTrue(AiOutputValidator.validate("   "));
    }

    @Test
    @DisplayName("Triệu đơn vị hợp lệ → safe")
    void shouldPassValidTrieuAmount() {
        assertTrue(AiOutputValidator.validate("Thu nhập 15 triệu mỗi tháng"));
    }

    @Test
    @DisplayName("Tỷ đơn vị lớn nhưng hợp lý (<= 100 tỷ) → safe")
    void shouldPassReasonableTyAmount() {
        assertTrue(AiOutputValidator.validate("Tổng tài sản 50 tỷ VND"));
    }
}
