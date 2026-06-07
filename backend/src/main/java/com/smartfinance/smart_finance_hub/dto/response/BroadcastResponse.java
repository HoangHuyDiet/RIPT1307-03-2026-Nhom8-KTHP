package com.smartfinance.smart_finance_hub.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BroadcastResponse {
    private Long key;
    private String title;
    private String content;
    private String target;
    private String urgency;
    private LocalDateTime time;
}
