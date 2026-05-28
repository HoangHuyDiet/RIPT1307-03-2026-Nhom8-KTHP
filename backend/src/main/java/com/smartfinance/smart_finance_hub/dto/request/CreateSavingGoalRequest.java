package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateSavingGoalRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    private BigDecimal targetAmount;

    private BigDecimal currentAmount = BigDecimal.ZERO;

    private String currency;

    private LocalDate dueDate;

    private Long categoryId;
}
