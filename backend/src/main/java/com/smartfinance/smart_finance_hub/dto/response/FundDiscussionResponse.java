package com.smartfinance.smart_finance_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundDiscussionResponse {

    private Long id;
    private String senderName;
    private String senderAvatar;
    private String type;
    private String text;
    private String time;
}


