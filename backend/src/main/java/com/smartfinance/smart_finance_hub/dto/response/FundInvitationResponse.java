package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundInvitationResponse {

    private Long invitationId;
    private Long fundId;
    private String fundName;
    private String invitedEmail;
    private String type;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public static FundInvitationResponse from(FundInvitation invitation) {
        return FundInvitationResponse.builder()
                .invitationId(invitation.getId())
                .fundId(invitation.getShareFund() != null ? invitation.getShareFund().getId() : null)
                .fundName(invitation.getShareFund() != null ? invitation.getShareFund().getName() : null)
                .invitedEmail(invitation.getInvitedEmail())
                .type(invitation.getType())
                .status(invitation.getStatus())
                .reason(null)
                .createdAt(invitation.getCreatedAt())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }
}


