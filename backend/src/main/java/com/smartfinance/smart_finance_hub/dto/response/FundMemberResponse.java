package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.entity.FundMember;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundMemberResponse {

    private Long memberId;
    private Long userId;
    private String displayName;
    private String email;
    private String fundRole;
    private LocalDateTime joinedAt;

    public static FundMemberResponse from(FundMember member) {
        return FundMemberResponse.builder()
                .memberId(member.getId())
                .userId(member.getUser().getId())
                .displayName(member.getUser().getDisplayName())
                .email(member.getUser().getEmail())
                .fundRole(member.getFundRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}


