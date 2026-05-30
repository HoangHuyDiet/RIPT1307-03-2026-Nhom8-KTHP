package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisbandFundRequest {

    @Size(max = 500, message = "Reason must be at most 500 characters")
    private String reason;
}


