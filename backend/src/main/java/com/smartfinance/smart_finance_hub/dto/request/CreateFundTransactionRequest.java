package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFundTransactionRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Type is required (INCOME/EXPENSE)")
    private String type;

    private String description;

    @NotNull(message = "Transaction date is required")
    private LocalDate date;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Long personalFundId;
}
