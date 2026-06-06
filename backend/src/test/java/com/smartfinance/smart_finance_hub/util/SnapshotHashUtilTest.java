package com.smartfinance.smart_finance_hub.util;

import com.smartfinance.smart_finance_hub.dto.response.FinancialSnapshotDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests cho SnapshotHashUtil — đảm bảo hash ổn định và null-safe.
 */
class SnapshotHashUtilTest {

    @Test
    @DisplayName("Cùng snapshot → cùng hash (deterministic)")
    void shouldProduceSameHashForSameInput() {
        FinancialSnapshotDTO snapshot = FinancialSnapshotDTO.builder()
            .month("2026-06")
            .totalIncome(new BigDecimal("15000000"))
            .totalExpense(new BigDecimal("10000000"))
            .build();

        String hash1 = SnapshotHashUtil.computeHash(snapshot);
        String hash2 = SnapshotHashUtil.computeHash(snapshot);

        assertNotNull(hash1);
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Snapshot khác nhau → hash khác nhau")
    void shouldProduceDifferentHashForDifferentInput() {
        FinancialSnapshotDTO s1 = FinancialSnapshotDTO.builder()
            .month("2026-06")
            .totalIncome(new BigDecimal("15000000"))
            .build();

        FinancialSnapshotDTO s2 = FinancialSnapshotDTO.builder()
            .month("2026-06")
            .totalIncome(new BigDecimal("20000000"))
            .build();

        String hash1 = SnapshotHashUtil.computeHash(s1);
        String hash2 = SnapshotHashUtil.computeHash(s2);

        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Hash là SHA-256 (64 hex chars)")
    void shouldProduceSha256Hash() {
        FinancialSnapshotDTO snapshot = FinancialSnapshotDTO.builder()
            .month("2026-06")
            .build();

        String hash = SnapshotHashUtil.computeHash(snapshot);
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("Map key order ổn định")
    void shouldProduceStableHashRegardlessOfMapInsertionOrder() {
        FinancialSnapshotDTO s1 = FinancialSnapshotDTO.builder()
            .month("2026-06")
            .expenseByCategory(Map.of("Ăn uống", new BigDecimal("3000000"),
                "Di chuyển", new BigDecimal("1000000")))
            .build();

        // Tạo lại cùng data
        FinancialSnapshotDTO s2 = FinancialSnapshotDTO.builder()
            .month("2026-06")
            .expenseByCategory(Map.of("Di chuyển", new BigDecimal("1000000"),
                "Ăn uống", new BigDecimal("3000000")))
            .build();

        String hash1 = SnapshotHashUtil.computeHash(s1);
        String hash2 = SnapshotHashUtil.computeHash(s2);

        assertEquals(hash1, hash2, "Hash phải ổn định bất kể thứ tự key");
    }
}
