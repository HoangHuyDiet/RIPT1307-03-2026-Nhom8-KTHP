package com.smartfinance.smart_finance_hub.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonalFundRequest {

    private String name;

    private String description;
}
