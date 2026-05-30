package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
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
public class UpdateFundRequest {

    @Size(max = 100, message = "Fund name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Positive(message = "Target amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Invalid amount")
    private BigDecimal targetAmount;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;
}


