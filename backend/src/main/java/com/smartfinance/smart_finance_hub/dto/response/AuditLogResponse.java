package com.smartfinance.smart_finance_hub.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long key;
    private LocalDateTime time;
    private String action;
    private String targetUser;
    private String ip;
    private String status;
}
