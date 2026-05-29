package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingGoalRequest {

    @NotBlank(message = "Goal name is required")
    @Size(max = 100, message = "Goal name must be at most 100 characters")
    private String name;

    @Positive(message = "Target amount must be greater than 0")
    private BigDecimal targetAmount;

    private LocalDate dueDate;
}
