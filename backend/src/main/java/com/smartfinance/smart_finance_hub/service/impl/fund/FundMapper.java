package com.smartfinance.smart_finance_hub.service.impl.fund;

import com.smartfinance.smart_finance_hub.dto.response.DisbandStatusResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundListResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundTransactionResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse;
import com.smartfinance.smart_finance_hub.entity.Fund;
import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import com.smartfinance.smart_finance_hub.entity.FundMessage;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.repository.FundMemberRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FundMapper {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd/MM");
    private static final String[] THEME_COLORS =
            {"#1890ff", "#52c41a", "#722ed1", "#fa8c16", "#13c2c2", "#eb2f96"};

    private final FundMemberRepository fundMemberRepository;
    private final UserRepository userRepository;

    public FeFundListResponse toFeFundListResponse(Fund fund) {
        List<FeFundListResponse.MemberSummary> members = fundMemberRepository.findByFundId(fund.getId())
                .stream()
                .map(member -> FeFundListResponse.MemberSummary.builder()
                        .name(member.getUser().getDisplayName())
                        .avatar(defaultAvatar(member.getUser()))
                        .role(member.getFundRole())
                        .email(member.getUser().getEmail())
                        .build())
                .collect(Collectors.toList());
        return FeFundListResponse.builder()
                .id(fund.getId())
                .name(fund.getName())
                .balance(fund.getBalance())
                .target(fund.getTargetAmount())
                .status(fund.getStatus() == null ? "active" : fund.getStatus().toLowerCase())
                .membersCount(members.size())
                .themeColor(resolveThemeColor(fund))
                .members(members)
                .build();
    }

    public FeFundTransactionResponse toFeTransactionResponse(Transaction transaction) {
        return FeFundTransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .userDisplayName(transaction.getUser().getDisplayName())
                .categoryName(transaction.getCategory().getName())
                .isApproved(transaction.getIsApproved())
                .status(transaction.getStatus())
                .build();
    }

    public FundDiscussionResponse toDiscussionResponse(FundMessage message) {
        User sender = message.getSender();
        String senderName = sender == null ? "System" : sender.getDisplayName();
        return FundDiscussionResponse.builder()
                .id(message.getId())
                .senderName(senderName)
                .senderAvatar(sender == null ? "" : defaultAvatar(sender))
                .type(message.getType())
                .text(message.getText())
                .time(formatTime(message.getCreatedAt()))
                .build();
    }

    public DisbandStatusResponse.MemberVote toMemberVote(FundInvitation invitation) {
        String displayName = userRepository.findByEmail(invitation.getInvitedEmail())
                .map(User::getDisplayName)
                .orElse(invitation.getInvitedEmail());
        return DisbandStatusResponse.MemberVote.builder()
                .email(invitation.getInvitedEmail())
                .displayName(displayName)
                .vote(invitation.getStatus())
                .build();
    }

    public double calculateProgressPercent(Fund fund) {
        if (fund.getTargetAmount() == null || fund.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        double percent = fund.getBalance().doubleValue() / fund.getTargetAmount().doubleValue() * 100;
        return Math.min(percent, 100);
    }

    public String resolveThemeColor(Fund fund) {
        if (fund.getThemeColor() != null && !fund.getThemeColor().isBlank()) {
            return fund.getThemeColor();
        }
        return themeColor(fund.getId());
    }

    public String themeColor(Long fundId) {
        return THEME_COLORS[(int) (Math.abs(fundId == null ? 0 : fundId) % THEME_COLORS.length)];
    }

    public String defaultAvatar(User user) {
        if (user == null || user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            return "";
        }
        return user.getDisplayName().trim().substring(0, 1).toUpperCase();
    }

    public String formatVnd(BigDecimal amount) {
        if (amount == null) {
            return "0\u0111";
        }
        return String.format("%,.0f\u0111", amount).replace(",", ".");
    }

    public String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMAT);
    }
}
