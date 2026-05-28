package com.smartfinance.smart_finance_hub.service.impl.fund;

import com.smartfinance.smart_finance_hub.dto.request.DisbandFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.InviteMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.KickMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondByTokenRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondInvitationRequest;
import com.smartfinance.smart_finance_hub.dto.response.DisbandStatusResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundInvitationResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundMemberResponse;
import com.smartfinance.smart_finance_hub.entity.Fund;
import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import com.smartfinance.smart_finance_hub.entity.FundMember;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.FundInvitationType;
import com.smartfinance.smart_finance_hub.enums.FundRole;
import com.smartfinance.smart_finance_hub.enums.FundStatus;
import com.smartfinance.smart_finance_hub.enums.InvitationStatus;
import com.smartfinance.smart_finance_hub.repository.FundInvitationRepository;
import com.smartfinance.smart_finance_hub.repository.FundMemberRepository;
import com.smartfinance.smart_finance_hub.repository.FundRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundMemberApplicationService {

    private final FundRepository fundRepository;
    private final FundInvitationRepository fundInvitationRepository;
    private final FundMemberRepository fundMemberRepository;
    private final UserRepository userRepository;
    private final FundAccessService access;
    private final FundMapper mapper;
    private final FundNotificationService notifications;

    @Transactional(readOnly = true)
    public List<FundMemberResponse> getMembers(Long fundId, Long userId) {
        access.requireFund(fundId);
        access.requireMember(fundId, userId);
        return fundMemberRepository.findByFundId(fundId).stream()
                .map(FundMemberResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public FundInvitationResponse inviteMember(
            Long fundId, InviteMemberRequest request, Long inviterUserId) {
        Fund fund = access.requireActiveFund(fundId);
        access.requireOwner(fundId, inviterUserId);

        String invitedEmail = request.getEmail().trim();
        User invitedUser = userRepository.findByEmail(invitedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + invitedEmail));

        if (fundMemberRepository.existsByFundIdAndUserId(fundId, invitedUser.getId())) {
            throw new IllegalStateException(invitedEmail + " is already a fund member");
        }
        if (fundInvitationRepository.existsByFundIdAndInvitedEmailIgnoreCaseAndTypeAndStatus(
                fundId, invitedEmail, FundInvitationType.MEMBER_INVITE.name(),
                InvitationStatus.PENDING.name())) {
            throw new IllegalStateException("A pending invitation already exists for: " + invitedEmail);
        }

        FundInvitation invitation = FundInvitation.builder()
                .fund(fund)
                .invitedEmail(invitedEmail)
                .invitationToken(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING.name())
                .type(FundInvitationType.MEMBER_INVITE.name())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        FundInvitation saved = fundInvitationRepository.save(invitation);

        User inviter = access.requireUser(inviterUserId);
        notifications.afterCommit(() -> notifications.sendFundInvitationEmail(saved, invitedUser, inviter, fund),
                "inviteMember:" + invitedEmail);

        return FundInvitationResponse.from(saved);
    }

    @Transactional
    public FundInvitationResponse kickMember(Long fundId, KickMemberRequest request, Long ownerUserId) {
        Fund fund = access.requireActiveFund(fundId);
        access.requireOwner(fundId, ownerUserId);

        String reason = access.normalizeRequired(request.getReason(), "Reason is required");
        String memberEmail = request.getMemberEmail().trim();

        FundMember targetMember = fundMemberRepository
                .findByFundIdAndUserEmailIgnoreCase(fundId, memberEmail)
                .orElseThrow(() -> new IllegalArgumentException(memberEmail + " is not a fund member"));

        if (targetMember.getUser().getId().equals(ownerUserId)) {
            throw new IllegalArgumentException("Owner cannot kick themself");
        }
        if (fundInvitationRepository.existsByFundIdAndInvitedEmailIgnoreCaseAndTypeAndStatus(
                fundId, memberEmail, FundInvitationType.KICK_PROPOSAL.name(),
                InvitationStatus.PENDING.name())) {
            throw new IllegalStateException("A pending kick proposal already exists for: " + memberEmail);
        }

        FundInvitation kickProposal = FundInvitation.builder()
                .fund(fund)
                .invitedEmail(memberEmail)
                .invitationToken(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING.name())
                .type(FundInvitationType.KICK_PROPOSAL.name())
                .reason(reason)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        FundInvitation saved = fundInvitationRepository.save(kickProposal);

        User owner = access.requireUser(ownerUserId);
        User targetUser = targetMember.getUser();
        notifications.afterCommit(() -> notifications.sendKickProposalEmail(saved, targetUser, owner, fund, reason),
                "kickMember:" + memberEmail);

        return FundInvitationResponse.from(saved);
    }

    @Transactional
    public FundInvitationResponse requestRemoveMember(Long fundId, String memberEmail, Long ownerUserId) {
        return kickMember(fundId, new KickMemberRequest(memberEmail, "Owner requested member removal"), ownerUserId);
    }

    @Transactional
    public void leaveFund(Long fundId, Long userId) {
        Fund fund = access.requireActiveFund(fundId);
        FundMember member = access.requireMember(fundId, userId);

        if (FundRole.OWNER.name().equals(member.getFundRole())) {
            throw new IllegalArgumentException("Owner cannot leave the fund. Disband it instead");
        }

        User user = access.requireUser(userId);
        access.cancelPendingInvitationsForEmail(fundId, user.getEmail());

        FundAccessService.Recipient owner = access.findOwnerRecipient(fundId);
        String fundName = fund.getName();
        String leavingName = user.getDisplayName();

        fundMemberRepository.delete(member);

        if (owner != null) {
            notifications.afterCommit(() -> notifications.sendFundNotificationEmail(
                    owner.getEmail(), owner.getName(), fundName, "Member left fund",
                    leavingName + " left fund " + fundName), "leaveFund:" + user.getEmail());
        }
    }

    @Transactional
    public void respondToInvitation(Long fundId, RespondInvitationRequest request, Long respondUserId) {
        FundInvitation invitation = fundInvitationRepository.findById(request.getInvitationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invitation not found: " + request.getInvitationId()));

        if (!invitation.getFund().getId().equals(fundId)) {
            throw new IllegalArgumentException("Invitation does not belong to this fund");
        }
        processInvitationResponse(invitation, request.getAction(), respondUserId);
    }

    @Transactional
    public void respondToInvitationByToken(
            String token, RespondByTokenRequest request, Long respondUserId) {
        FundInvitation invitation = fundInvitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));
        processInvitationResponse(invitation, request.getAction(), respondUserId);
    }

    @Transactional
    public DisbandStatusResponse proposeDisbandFund(
            Long fundId, DisbandFundRequest request, Long ownerUserId) {
        Fund fund = access.requireActiveFund(fundId);
        access.requireOwner(fundId, ownerUserId);

        boolean hasPendingDisband = !fundInvitationRepository.findByFundIdAndTypeAndStatus(
                fundId, FundInvitationType.DISBAND_PROPOSAL.name(),
                InvitationStatus.PENDING.name()).isEmpty();
        if (hasPendingDisband) {
            throw new IllegalStateException("A pending disband proposal already exists");
        }

        User owner = access.requireUser(ownerUserId);
        String reason = request.getReason() == null ? "" : request.getReason().trim();

        List<FundMember> nonOwnerMembers = fundMemberRepository.findByFundId(fundId).stream()
                .filter(member -> !FundRole.OWNER.name().equals(member.getFundRole()))
                .collect(Collectors.toList());

        if (nonOwnerMembers.isEmpty()) {
            fund.setStatus(FundStatus.DISBANDED.name());
            fundRepository.save(fund);
            return DisbandStatusResponse.builder()
                    .fundId(fundId)
                    .fundName(fund.getName())
                    .totalMembers(1)
                    .accepted(1)
                    .rejected(0)
                    .pending(0)
                    .cancelled(0)
                    .disbandStatus("APPROVED")
                    .votes(List.of())
                    .build();
        }

        List<DisbandProposalEmail> proposalEmails = new ArrayList<>();
        for (FundMember member : nonOwnerMembers) {
            FundInvitation proposal = FundInvitation.builder()
                    .fund(fund)
                    .invitedEmail(member.getUser().getEmail())
                    .invitationToken(UUID.randomUUID().toString())
                    .status(InvitationStatus.PENDING.name())
                    .type(FundInvitationType.DISBAND_PROPOSAL.name())
                    .reason(reason)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build();
            FundInvitation saved = fundInvitationRepository.save(proposal);
            proposalEmails.add(new DisbandProposalEmail(
                    member.getUser().getEmail(),
                    member.getUser().getDisplayName(),
                    saved.getInvitationToken()));
        }

        String fundName = fund.getName();
        String ownerName = owner.getDisplayName();
        notifications.afterCommit(() -> {
            for (DisbandProposalEmail email : proposalEmails) {
                notifications.sendDisbandProposalEmail(
                        email.getEmail(), email.getName(), fundName, ownerName, reason, email.getToken());
            }
        }, "disbandProposal:fund" + fundId);

        return getDisbandStatus(fundId, ownerUserId);
    }

    @Transactional(readOnly = true)
    public DisbandStatusResponse getDisbandStatus(Long fundId, Long userId) {
        Fund fund = access.requireFund(fundId);
        access.requireMember(fundId, userId);

        List<FundInvitation> invitations = fundInvitationRepository
                .findByFundIdAndType(fundId, FundInvitationType.DISBAND_PROPOSAL.name());

        int accepted = 0;
        int rejected = 0;
        int pending = 0;
        int cancelled = 0;
        for (FundInvitation invitation : invitations) {
            if (InvitationStatus.ACCEPTED.name().equals(invitation.getStatus())) {
                accepted++;
            } else if (InvitationStatus.REJECTED.name().equals(invitation.getStatus())) {
                rejected++;
            } else if (InvitationStatus.PENDING.name().equals(invitation.getStatus())) {
                pending++;
            } else if (InvitationStatus.CANCELLED.name().equals(invitation.getStatus())) {
                cancelled++;
            }
        }

        boolean hasAnyDisbandProposal = !invitations.isEmpty();
        boolean isFundDisbanded = FundStatus.DISBANDED.name().equals(fund.getStatus());
        int ownerVote = (hasAnyDisbandProposal || isFundDisbanded) ? 1 : 0;

        String disbandStatus;
        if (isFundDisbanded) {
            disbandStatus = "APPROVED";
        } else if (!hasAnyDisbandProposal) {
            disbandStatus = "NONE";
        } else if (rejected > 0) {
            disbandStatus = "REJECTED";
        } else {
            disbandStatus = "PENDING";
        }

        List<DisbandStatusResponse.MemberVote> votes = invitations.stream()
                .map(mapper::toMemberVote)
                .collect(Collectors.toList());

        return DisbandStatusResponse.builder()
                .fundId(fundId)
                .fundName(fund.getName())
                .totalMembers(access.getMemberCount(fundId))
                .accepted(accepted + ownerVote)
                .rejected(rejected)
                .pending(pending)
                .cancelled(cancelled)
                .disbandStatus(disbandStatus)
                .votes(votes)
                .build();
    }

    @Transactional
    public List<FundInvitationResponse> getMyPendingInvitations(Long userId) {
        User user = access.requireUser(userId);
        List<FundInvitation> pendingInvitations = fundInvitationRepository
                .findByInvitedEmailIgnoreCaseAndStatus(user.getEmail(), InvitationStatus.PENDING.name());

        List<FundInvitation> expired = new ArrayList<>();
        List<FundInvitation> valid = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (FundInvitation invitation : pendingInvitations) {
            if (invitation.getExpiresAt().isBefore(now)) {
                invitation.setStatus(InvitationStatus.EXPIRED.name());
                expired.add(invitation);
            } else {
                valid.add(invitation);
            }
        }
        if (!expired.isEmpty()) {
            fundInvitationRepository.saveAll(expired);
        }
        return valid.stream()
                .map(FundInvitationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FundInvitationResponse> getFundInvitations(Long fundId, Long userId) {
        access.requireFund(fundId);
        access.requireOwner(fundId, userId);
        return fundInvitationRepository.findByFundId(fundId).stream()
                .map(FundInvitationResponse::from)
                .collect(Collectors.toList());
    }

    private void processInvitationResponse(
            FundInvitation invitation, String rawAction, Long respondUserId) {
        access.validateInvitationPending(invitation);

        User respondUser = access.requireUser(respondUserId);
        if (!respondUser.getEmail().equalsIgnoreCase(invitation.getInvitedEmail())) {
            throw new IllegalArgumentException("You are not the requested user");
        }

        String action = access.normalizeAction(rawAction);
        String type = invitation.getType();
        if (FundInvitationType.MEMBER_INVITE.name().equals(type)) {
            handleMemberInviteResponse(invitation, respondUser, action);
        } else if (FundInvitationType.KICK_PROPOSAL.name().equals(type)) {
            handleKickProposalResponse(invitation, respondUser, action);
        } else if (FundInvitationType.DISBAND_PROPOSAL.name().equals(type)) {
            handleDisbandProposalResponse(invitation, respondUser, action);
        } else {
            throw new IllegalArgumentException("Invalid invitation type: " + type);
        }
    }

    private void handleMemberInviteResponse(FundInvitation invitation, User respondUser, String action) {
        Fund fund = invitation.getFund();
        if (FundStatus.DISBANDED.name().equals(fund.getStatus())) {
            invitation.setStatus(InvitationStatus.CANCELLED.name());
            fundInvitationRepository.save(invitation);
            throw new IllegalStateException("Fund is already disbanded");
        }

        if ("ACCEPT".equals(action)) {
            if (fundMemberRepository.existsByFundIdAndUserId(fund.getId(), respondUser.getId())) {
                invitation.setStatus(InvitationStatus.CANCELLED.name());
                fundInvitationRepository.save(invitation);
                throw new IllegalStateException("You are already a member of this fund");
            }

            invitation.setStatus(InvitationStatus.ACCEPTED.name());
            fundInvitationRepository.save(invitation);

            FundMember newMember = FundMember.builder()
                    .fund(fund)
                    .user(respondUser)
                    .fundRole(FundRole.MEMBER.name())
                    .build();
            fundMemberRepository.save(newMember);
        } else {
            invitation.setStatus(InvitationStatus.REJECTED.name());
            fundInvitationRepository.save(invitation);
        }
    }

    private void handleKickProposalResponse(FundInvitation invitation, User respondUser, String action) {
        Fund fund = invitation.getFund();
        if (FundStatus.DISBANDED.name().equals(fund.getStatus())) {
            invitation.setStatus(InvitationStatus.CANCELLED.name());
            fundInvitationRepository.save(invitation);
            throw new IllegalStateException("Fund is already disbanded");
        }

        if (!fundMemberRepository.existsByFundIdAndUserId(fund.getId(), respondUser.getId())) {
            invitation.setStatus(InvitationStatus.CANCELLED.name());
            fundInvitationRepository.save(invitation);
            throw new IllegalStateException("You are no longer a member of this fund");
        }

        if ("ACCEPT".equals(action)) {
            invitation.setStatus(InvitationStatus.ACCEPTED.name());
            fundInvitationRepository.save(invitation);

            FundMember member = access.requireMember(fund.getId(), respondUser.getId());
            access.cancelPendingInvitationsForEmail(fund.getId(), respondUser.getEmail());
            fundMemberRepository.delete(member);

            notifications.notifyOwnerAfterCommit(fund, "Member left fund",
                    respondUser.getDisplayName() + " accepted the kick proposal for " + fund.getName());
        } else {
            invitation.setStatus(InvitationStatus.REJECTED.name());
            fundInvitationRepository.save(invitation);

            notifications.notifyOwnerAfterCommit(fund, "Kick proposal rejected",
                    respondUser.getDisplayName() + " rejected the kick proposal for " + fund.getName());
        }
    }

    private void handleDisbandProposalResponse(FundInvitation invitation, User respondUser, String action) {
        Fund fund = invitation.getFund();
        if ("ACCEPT".equals(action)) {
            invitation.setStatus(InvitationStatus.ACCEPTED.name());
            fundInvitationRepository.save(invitation);

            List<FundInvitation> remainingPending = fundInvitationRepository.findByFundIdAndTypeAndStatus(
                    fund.getId(), FundInvitationType.DISBAND_PROPOSAL.name(), InvitationStatus.PENDING.name());

            if (remainingPending.isEmpty()) {
                fund.setStatus(FundStatus.DISBANDED.name());
                fundRepository.save(fund);

                List<FundAccessService.Recipient> recipients = fundMemberRepository.findByFundId(fund.getId())
                        .stream()
                        .map(member -> new FundAccessService.Recipient(
                                member.getUser().getEmail(), member.getUser().getDisplayName()))
                        .collect(Collectors.toList());
                String fundName = fund.getName();

                notifications.afterCommit(() -> {
                    for (FundAccessService.Recipient recipient : recipients) {
                        notifications.sendDisbandConfirmationEmail(recipient, fundName);
                    }
                }, "disbandConfirm:fund" + fund.getId());
            }
        } else {
            invitation.setStatus(InvitationStatus.REJECTED.name());
            fundInvitationRepository.save(invitation);

            List<FundInvitation> remainingPending = fundInvitationRepository.findByFundIdAndTypeAndStatus(
                    fund.getId(), FundInvitationType.DISBAND_PROPOSAL.name(), InvitationStatus.PENDING.name());
            for (FundInvitation pending : remainingPending) {
                pending.setStatus(InvitationStatus.CANCELLED.name());
            }
            if (!remainingPending.isEmpty()) {
                fundInvitationRepository.saveAll(remainingPending);
            }

            notifications.notifyOwnerAfterCommit(fund, "Disband proposal rejected",
                    respondUser.getDisplayName() + " rejected disbanding fund " + fund.getName());
        }
    }

    private static class DisbandProposalEmail {
        private final String email;
        private final String name;
        private final String token;

        DisbandProposalEmail(String email, String name, String token) {
            this.email = email;
            this.name = name;
            this.token = token;
        }

        String getEmail() {
            return email;
        }

        String getName() {
            return name;
        }

        String getToken() {
            return token;
        }
    }
}
