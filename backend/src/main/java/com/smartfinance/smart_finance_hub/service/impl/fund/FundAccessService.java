package com.smartfinance.smart_finance_hub.service.impl.fund;

import com.smartfinance.smart_finance_hub.entity.Fund;
import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import com.smartfinance.smart_finance_hub.entity.FundMember;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.FundRole;
import com.smartfinance.smart_finance_hub.enums.FundStatus;
import com.smartfinance.smart_finance_hub.enums.InvitationStatus;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.FundInvitationRepository;
import com.smartfinance.smart_finance_hub.repository.FundMemberRepository;
import com.smartfinance.smart_finance_hub.repository.FundRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundAccessService {

    private final FundRepository fundRepository;
    private final FundMemberRepository fundMemberRepository;
    private final UserRepository userRepository;
    private final FundInvitationRepository fundInvitationRepository;

    public Fund requireFund(Long fundId) {
        return fundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Fund not found: " + fundId));
    }

    public Fund requireActiveFund(Long fundId) {
        Fund fund = requireFund(fundId);
        if (FundStatus.DISBANDED.name().equals(fund.getStatus())) {
            throw new IllegalStateException("Fund is already disbanded: " + fund.getName());
        }
        return fund;
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public FundMember requireMember(Long fundId, Long userId) {
        return fundMemberRepository.findByFundIdAndUserId(fundId, userId)
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this fund"));
    }

    public FundMember requireOwner(Long fundId, Long userId) {
        FundMember member = requireMember(fundId, userId);
        if (!FundRole.OWNER.name().equals(member.getFundRole())) {
            throw new IllegalArgumentException("Only the fund owner can perform this action");
        }
        return member;
    }

    public void validateInvitationPending(FundInvitation invitation) {
        if (!InvitationStatus.PENDING.name().equals(invitation.getStatus())) {
            throw new IllegalStateException("Invitation was already processed: " + invitation.getStatus());
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED.name());
            fundInvitationRepository.save(invitation);
            throw new IllegalStateException("Invitation expired");
        }
    }

    public String getMyRole(Long fundId, Long userId) {
        return fundMemberRepository.findByFundIdAndUserId(fundId, userId)
                .map(FundMember::getFundRole)
                .orElse(null);
    }

    public int getMemberCount(Long fundId) {
        return fundMemberRepository.findByFundId(fundId).size();
    }

    public String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public String normalizeAction(String action) {
        String normalized = normalizeRequired(action, "Action is required").toUpperCase();
        if (!"ACCEPT".equals(normalized) && !"REJECT".equals(normalized)) {
            throw new IllegalArgumentException("Action must be ACCEPT or REJECT");
        }
        return normalized;
    }

    public TransactionType parseTransactionType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Transaction type must be INCOME or EXPENSE");
        }
    }

    public void cancelPendingInvitationsForEmail(Long fundId, String email) {
        List<FundInvitation> pendingInvitations =
                fundInvitationRepository.findByFundIdAndInvitedEmailIgnoreCaseAndStatus(
                        fundId, email, InvitationStatus.PENDING.name());
        for (FundInvitation invitation : pendingInvitations) {
            invitation.setStatus(InvitationStatus.CANCELLED.name());
        }
        if (!pendingInvitations.isEmpty()) {
            fundInvitationRepository.saveAll(pendingInvitations);
        }
    }

    public Recipient findOwnerRecipient(Long fundId) {
        return fundMemberRepository.findByFundId(fundId).stream()
                .filter(member -> FundRole.OWNER.name().equals(member.getFundRole()))
                .findFirst()
                .map(member -> new Recipient(
                        member.getUser().getEmail(), member.getUser().getDisplayName()))
                .orElse(null);
    }

    public static class Recipient {
        private final String email;
        private final String name;

        public Recipient(String email, String name) {
            this.email = email;
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }
    }
}
