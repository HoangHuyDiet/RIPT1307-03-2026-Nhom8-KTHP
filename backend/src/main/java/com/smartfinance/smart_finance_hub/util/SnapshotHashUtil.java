package com.smartfinance.smart_finance_hub.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tính SHA-256 hash từ FinancialSnapshotDTO (ổn định key order).
 * Dùng để kiểm tra snapshot thay đổi — quyết định có gọi Gemini hay trả cache.
 */
@Slf4j
public final class SnapshotHashUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    private SnapshotHashUtil() {}

    public static String computeHash(Object snapshot) {
        try {
            String json = MAPPER.writeValueAsString(snapshot);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            log.error("Lỗi khi tính snapshot hash", e);
            // Trả về null — caller sẽ coi là cache miss
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
