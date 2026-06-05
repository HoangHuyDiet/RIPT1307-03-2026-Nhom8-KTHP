package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecurringSettingRequest {

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotBlank(message = "Loại giao dịch không được để trống (INCOME/EXPENSE)")
    private String type;

    private String description;

    @NotBlank(message = "Tần suất không được để trống (DAILY/WEEKLY/MONTHLY)")
    private String frequency;

    private Integer dayOfMonth;

    private Integer dayOfWeek;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
}
