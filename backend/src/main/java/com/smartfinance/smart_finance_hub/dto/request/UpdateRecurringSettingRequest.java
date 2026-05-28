package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecurringSettingRequest {

    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    private String type;

    private String description;

    private String frequency;

    private Integer dayOfMonth;

    private Integer dayOfWeek;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long categoryId;
}
