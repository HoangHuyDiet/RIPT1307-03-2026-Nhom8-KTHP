package com.smartfinance.smart_finance_hub.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LockRequestResponse {
    private Long id;
    private String email;
    private String name;
    private String reason;
    private String status;
    private LocalDateTime time;
}
