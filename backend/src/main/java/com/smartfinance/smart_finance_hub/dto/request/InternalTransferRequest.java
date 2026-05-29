package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternalTransferRequest {

    @NotNull(message = "Source fund ID is required")
    private Long fromFundId;

    @NotNull(message = "Destination fund ID is required")
    private Long toFundId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;
}
