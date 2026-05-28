package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.entity.RecurringSetting;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringSettingResponse {

    private Long id;
    private BigDecimal amount;
    private String type;
    private String description;
    private String frequency;
    private Integer dayOfMonth;
    private Integer dayOfWeek;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextRunDate;
    private Boolean isActive;
    private LocalDateTime lastRunAt;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime createdAt;

    public static RecurringSettingResponse from(RecurringSetting setting) {
        return RecurringSettingResponse.builder()
                .id(setting.getId())
                .amount(setting.getAmount())
                .type(setting.getType())
                .description(setting.getDescription())
                .frequency(setting.getFrequency().name())
                .dayOfMonth(setting.getDayOfMonth())
                .dayOfWeek(setting.getDayOfWeek())
                .startDate(setting.getStartDate())
                .endDate(setting.getEndDate())
                .nextRunDate(setting.getNextRunDate())
                .isActive(setting.getIsActive())
                .lastRunAt(setting.getLastRunAt())
                .categoryName(setting.getCategory().getName())
                .categoryId(setting.getCategory().getId())
                .createdAt(setting.getCreatedAt())
                .build();
    }
}
