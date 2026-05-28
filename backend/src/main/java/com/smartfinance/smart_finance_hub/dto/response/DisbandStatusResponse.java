package com.smartfinance.smart_finance_hub.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbandStatusResponse {

    private Long fundId;
    private String fundName;
    private int totalMembers;
    private int accepted;
    private int rejected;
    private int pending;
    private int cancelled;
    private String disbandStatus;
    private List<MemberVote> votes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberVote {
        private String email;
        private String displayName;
        private String vote;
    }
}


