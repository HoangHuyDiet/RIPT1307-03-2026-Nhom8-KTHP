package com.smartfinance.smart_finance_hub.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiRateLimitService {

    private final Map<Long, Deque<Instant>> chatRequestsByUser = new ConcurrentHashMap<>();

    @Value("${ai.rate-limit.chat-per-minute:10}")
    private int chatPerMinute;

    public boolean allowChat(Long userId) {
        Instant now = Instant.now();
        Instant cutoff = now.minus(Duration.ofMinutes(1));
        Deque<Instant> requests = chatRequestsByUser.computeIfAbsent(userId, ignored -> new ArrayDeque<>());

        synchronized (requests) {
            while (!requests.isEmpty() && requests.peekFirst().isBefore(cutoff)) {
                requests.removeFirst();
            }
            if (requests.size() >= chatPerMinute) {
                return false;
            }
            requests.addLast(now);
            return true;
        }
    }
}
