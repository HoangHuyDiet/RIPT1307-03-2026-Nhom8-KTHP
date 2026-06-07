package com.smartfinance.smart_finance_hub.dto.request;

import com.smartfinance.smart_finance_hub.enums.ConsentScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationCreateRequest {

    @NotBlank(message = "Câu hỏi không được để trống")
    @Size(max = 5000, message = "Câu hỏi tối đa 5000 ký tự")
    private String question;

    @NotEmpty(message = "Phải chọn ít nhất một phạm vi dữ liệu")
    private Set<ConsentScope> consentScopes;
}
