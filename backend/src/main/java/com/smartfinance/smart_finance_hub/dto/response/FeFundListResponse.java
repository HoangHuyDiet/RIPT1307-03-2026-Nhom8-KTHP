package com.smartfinance.smart_finance_hub.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeFundListResponse {

    private Long id;
    private String name;
    private BigDecimal balance;
    private BigDecimal target;
    private String status;
    private int membersCount;
    private String themeColor;
    private List<MemberSummary> members;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSummary {
        private String name;
        private String avatar;
        private String role;
        private String email;
    }
}


