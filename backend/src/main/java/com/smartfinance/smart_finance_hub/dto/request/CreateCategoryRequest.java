package com.smartfinance.smart_finance_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Ten danh muc khong duoc de trong")
    private String name;

    @NotBlank(message = "Loai danh muc khong duoc de trong (INCOME/EXPENSE)")
    private String type;

    private String description;
}
