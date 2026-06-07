package com.smartfinance.smart_finance_hub.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SupportUserResponse {
    private Long id;
    private String email;
    private String name;
    private String status;
    private String role;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
