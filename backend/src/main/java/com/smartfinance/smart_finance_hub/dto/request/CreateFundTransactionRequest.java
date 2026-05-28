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

    @NotNull(message = "Sá»‘ tiá»n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Positive(message = "Sá»‘ tiá»n pháº£i lá»›n hÆ¡n 0")
    private BigDecimal amount;

    @NotBlank(message = "Loáº¡i giao dá»‹ch khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng (INCOME/EXPENSE)")
    private String type; // "INCOME" hoáº·c "EXPENSE"

    private String description;

    @NotNull(message = "NgÃ y giao dá»‹ch khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate date;

    @NotNull(message = "Danh má»¥c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long categoryId;
}


