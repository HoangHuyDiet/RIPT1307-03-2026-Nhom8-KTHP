package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.enums.ConsultationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationDTO {

    private Long id;
    private Long userId;
    private String userQuestion;
    private String consentScope;
    private ConsultationStatus status;
    private String aiDraftSummary;
    private String finalAdvice;
    private Long advisorId;
    private String advisorName;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
}
