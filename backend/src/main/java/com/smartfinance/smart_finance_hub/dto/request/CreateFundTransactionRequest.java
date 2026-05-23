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

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotBlank(message = "Loại giao dịch không được để trống (INCOME/EXPENSE)")
    private String type; // "INCOME" hoặc "EXPENSE"

    private String description;

    @NotNull(message = "Ngày giao dịch không được để trống")
    private LocalDate date;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
}
