package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenameFundRequest {

    @NotNull(message = "fundId is required")
    private Long fundId;

    @NotBlank(message = "newName is required")
    @Size(max = 100, message = "newName must be at most 100 characters")
    private String newName;
}


