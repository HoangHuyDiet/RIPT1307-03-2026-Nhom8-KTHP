package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePersonalFundRequest {

    @NotBlank(message = "Tên quỹ không được để trống")
    private String name;

    @NotBlank(message = "Loại quỹ không được để trống (CASH, BANK_ACCOUNT, CREDIT_CARD, E_WALLET, INVESTMENT)")
    private String fundType;

    @PositiveOrZero(message = "Số dư ban đầu phải >= 0")
    private BigDecimal initialBalance;

    private String currency;

    private String description;
}
