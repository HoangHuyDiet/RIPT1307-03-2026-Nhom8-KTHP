package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.InviteMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondInvitationRequest;
import com.smartfinance.smart_finance_hub.entity.*;
import com.smartfinance.smart_finance_hub.enums.FundStatus;
import com.smartfinance.smart_finance_hub.enums.FundRole;
import com.smartfinance.smart_finance_hub.enums.InvitationStatus;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.*;
import com.smartfinance.smart_finance_hub.service.SharedFundService;
import com.smartfinance.smart_finance_hub.service.MailService;
import com.smartfinance.smart_finance_hub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedFundServiceImpl implements SharedFundService {

    private final ShareFundRepository shareFundRepository;
    private final FundInvitationRepository fundInvitationRepository;
    private final FundMemberRepository fundMemberRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final PersonalFundRepository personalFundRepository;
    private final FundMessageRepository fundMessageRepository;
    private final FundActivityRepository fundActivityRepository;
    private final MailService mailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public FundInvitation inviteMember(Long fundId, InviteMemberRequest request, Long inviterUserId) {
        log.info("inviteMember param: fundId={}, email={}", fundId, request.getEmail());

        ShareFund fund = shareFundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quỹ với ID: " + fundId));

        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người dùng với email: " + request.getEmail()));

        boolean alreadyMember = fundMemberRepository.existsByShareFundIdAndUserId(fundId, invitedUser.getId());
        if (alreadyMember) {
            throw new IllegalStateException("Người dùng " + request.getEmail() + " đã là thành viên của quỹ này!");
        }

        List<FundInvitation> pendingInvitations =
                fundInvitationRepository.findByShareFundIdAndStatus(fundId, InvitationStatus.PENDING.name());

        for (FundInvitation existing : pendingInvitations) {
            if (existing.getInvitedEmail().equalsIgnoreCase(request.getEmail())) {
                existing.setStatus("CANCELLED");
                fundInvitationRepository.save(existing);
                log.info("Đã hủy lời mời cũ (id={}) cho email: {}", existing.getId(), request.getEmail());
            }
        }

        FundInvitation invitation = FundInvitation.builder()
                .shareFund(fund)
                .invitedEmail(request.getEmail())
                .invitationToken(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING.name())
                .type("MEMBER_INVITE")
                .expiresAt(LocalDateTime.now().plusHours(12))
                .build();

        FundInvitation saved = fundInvitationRepository.save(invitation);
        log.info("inviteMember success: invitationId={}", saved.getId());

        String invitedEmail = invitedUser.getEmail();
        String finalInvitedName = invitedUser.getDisplayName() != null ? invitedUser.getDisplayName() : invitedUser.getEmail();
        User inviter = userRepository.findById(inviterUserId).orElse(null);
        String finalInviterName = inviter != null ? (inviter.getDisplayName() != null ? inviter.getDisplayName() : inviter.getEmail()) : "Thành viên";
        final String finalInviterEmail = inviter != null ? inviter.getEmail() : "";
        String finalFundName = fund.getName();
        String token = invitation.getInvitationToken();

        try {
            notificationService.createAndSendNotification(
                    invitedUser,
                    "FUND_INVITATION",
                    fund.getId(),
                    fund.getName(),
                    null,
                    "Bạn nhận được lời mời tham gia quỹ nhóm " + fund.getName() + " từ " + finalInviterName,
                    finalInviterName,
                    null,
                    null,
                    "MEMBER",
                    "/shared-funds"
            );
        } catch (Exception e) {
            log.error("Lỗi gửi thông báo in-app: {}", e.getMessage());
        }

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                mailService.sendFundInvitationEmail(
                        invitedEmail,
                        finalInvitedName,
                        finalInviterName,
                        finalInviterEmail,
                        finalFundName,
                        token,
                        12
                );
                log.info("Đã gửi email mời tham gia quỹ nhóm đến {}", invitedEmail);
            } catch (Exception e) {
                log.error("Lỗi khi gửi email mời tham gia quỹ nhóm đến {}: {}", invitedEmail, e.getMessage());
            }
        });

        return saved;
    }

    @Override
    @Transactional
    public void respondToInvitation(Long fundId, RespondInvitationRequest request, Long respondUserId) {
        log.info("respondToInvitation param: fundId={}, invitationId={}, action={}", fundId, request.getInvitationId(), request.getAction());

        FundInvitation invitation = fundInvitationRepository.findById(request.getInvitationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lời mời với ID: " + request.getInvitationId()));

        if (!invitation.getShareFund().getId().equals(fundId)) {
            throw new IllegalArgumentException("Lời mời không thuộc quỹ này!");
        }

        if (!InvitationStatus.PENDING.name().equals(invitation.getStatus())) {
            throw new IllegalStateException("Lời mời này đã được xử lý trước đó! Trạng thái: " + invitation.getStatus());
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED.name());
            fundInvitationRepository.save(invitation);
            throw new IllegalStateException("Lời mời đã hết hạn!");
        }

        User respondUser = userRepository.findById(respondUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        if (!respondUser.getEmail().equalsIgnoreCase(invitation.getInvitedEmail())) {
            throw new IllegalArgumentException("Bạn không phải người được mời!");
        }

        String action = request.getAction().toUpperCase();

        if ("ACCEPT".equals(action)) {
            invitation.setStatus(InvitationStatus.ACCEPTED.name());
            fundInvitationRepository.save(invitation);

            FundMember newMember = FundMember.builder()
                    .shareFund(invitation.getShareFund())
                    .user(respondUser)
                    .fundRole(FundRole.MEMBER.name())
                    .build();

            fundMemberRepository.save(newMember);
            log.info("respondToInvitation success: ACCEPTED - userId={} đã gia nhập quỹ {}", respondUserId, fundId);

        } else if ("REJECT".equals(action)) {
            invitation.setStatus(InvitationStatus.REJECTED.name());
            fundInvitationRepository.save(invitation);
            log.info("respondToInvitation success: REJECTED - userId={} đã từ chối lời mời quỹ {}", respondUserId, fundId);

        } else {
            throw new IllegalArgumentException("Hành động không hợp lệ! Chỉ chấp nhận ACCEPT hoặc REJECT.");
        }
    }

    @Override
    @Transactional
    public Transaction createFundTransaction(Long fundId, CreateFundTransactionRequest request, Long userId) {
        log.info("createFundTransaction param: fundId={}, userId={}, type={}, amount={}", fundId, userId, request.getType(), request.getAmount());

        ShareFund fund = shareFundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quỹ với ID: " + fundId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        boolean isMember = false;
        List<FundMember> members = fundMemberRepository.findByShareFundId(fundId);
        for (FundMember member : members) {
            if (member.getUser().getId().equals(userId)) {
                isMember = true;
                break;
            }
        }

        if (!isMember && !fund.getCreatedByUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không phải thành viên của quỹ này!");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Loại giao dịch không hợp lệ! Chỉ chấp nhận INCOME hoặc EXPENSE.");
        }

        validateFundCategory(category, transactionType.name(), userId);

        PersonalFund personalFund = null;
        if (request.getPersonalFundId() != null) {
            personalFund = findActiveUserPersonalFund(request.getPersonalFundId(), userId);
        }

        boolean autoApprove = (members.size() <= 1);

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .shareFund(fund)
                .personalFund(personalFund)
                .amount(request.getAmount())
                .type(transactionType.name())
                .description(request.getDescription())
                .date(request.getDate())
                .isApproved(autoApprove)
                .status(autoApprove ? "APPROVED" : "PENDING")
                .approvedByUser(autoApprove ? user : null)
                .approvedAt(autoApprove ? java.time.LocalDateTime.now() : null)
                .build();

        if (autoApprove) {
            java.math.BigDecimal currentBalance = fund.getBalance() != null ? fund.getBalance() : java.math.BigDecimal.ZERO;
            if (TransactionType.INCOME == transactionType) {
                fund.setBalance(currentBalance.add(request.getAmount()));
            } else if (TransactionType.EXPENSE == transactionType) {
                fund.setBalance(currentBalance.subtract(request.getAmount()));
            }
            applyPersonalFundImpact(transaction);
            shareFundRepository.save(fund);
        }

        Transaction saved = transactionRepository.save(transaction);
        log.info("createFundTransaction success: transactionId={}, isApproved={}", saved.getId(), autoApprove);

        if (!autoApprove) {
            String requesterName = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail();
            String type = "INCOME".equalsIgnoreCase(transactionType.name()) ? "DEPOSIT_REQUEST" : "WITHDRAW_REQUEST";
            String description = ("INCOME".equalsIgnoreCase(transactionType.name()) ? "Yêu cầu nạp tiền: " : "Yêu cầu rút tiền: ") + request.getDescription();
            for (FundMember fm : members) {
                if ("OWNER".equalsIgnoreCase(fm.getFundRole())) {
                    try {
                        notificationService.createAndSendNotification(
                                fm.getUser(),
                                type,
                                fund.getId(),
                                fund.getName(),
                                request.getAmount(),
                                description,
                                requesterName,
                                personalFund != null ? personalFund.getName() : null,
                                null,
                                "OWNER",
                                "/shared-funds"
                        );
                    } catch (Exception e) {
                        log.error("Failed to send transaction request notification", e);
                    }
                }
            }
        }

        return saved;
    }

    @Override
    @Transactional
    public Transaction approveTransaction(Long transactionId, Long approverUserId) {
        log.info("approveTransaction param: transactionId={}, approverUserId={}", transactionId, approverUserId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy giao dịch với ID: " + transactionId));

        ShareFund fund = transaction.getShareFund();
        if (fund == null) {
            throw new IllegalArgumentException("Giao dịch này không thuộc quỹ chung nào!");
        }

        boolean isOwner = false;

        if (fund.getCreatedByUser().getId().equals(approverUserId)) {
            isOwner = true;
        }

        if (!isOwner) {
            List<FundMember> members = fundMemberRepository.findByShareFundId(fund.getId());
            for (FundMember member : members) {
                if (member.getUser().getId().equals(approverUserId)
                        && FundRole.OWNER.name().equals(member.getFundRole())) {
                    isOwner = true;
                    break;
                }
            }
        }

        if (!isOwner) {
            throw new IllegalArgumentException("Chỉ Chủ quỹ (Owner) mới có quyền phê duyệt giao dịch!");
        }

        if (Boolean.TRUE.equals(transaction.getIsApproved())) {
            throw new IllegalStateException("Giao dịch này đã được phê duyệt trước đó!");
        }

        transaction.setIsApproved(true);
        transactionRepository.save(transaction);

        BigDecimal currentBalance = fund.getBalance();
        TransactionType txType = TransactionType.valueOf(transaction.getType());

        if (TransactionType.INCOME == txType) {
            fund.setBalance(currentBalance.add(transaction.getAmount()));
            log.info("INCOME: balance +{} -> new balance={}", transaction.getAmount(), fund.getBalance());
        } else if (TransactionType.EXPENSE == txType) {
            fund.setBalance(currentBalance.subtract(transaction.getAmount()));
            log.info("EXPENSE: balance -{} -> new balance={}", transaction.getAmount(), fund.getBalance());
        }

        applyPersonalFundImpact(transaction);
        shareFundRepository.save(fund);

        log.info("approveTransaction success: transactionId={}, isApproved=true, newBalance={}", transactionId, fund.getBalance());
        return transaction;
    }

    @Override
    @Transactional
    public Transaction approveOrRejectTransaction(Long transactionId, String action, String rejectReason, Long userId) {
        log.info("approveOrRejectTransaction param: transactionId={}, action={}, userId={}", transactionId, action, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch với ID: " + transactionId));

        ShareFund fund = transaction.getShareFund();
        if (fund == null) {
            throw new IllegalArgumentException("Giao dịch này không thuộc quỹ chung nào!");
        }

        boolean isOwner = fund.getCreatedByUser().getId().equals(userId);
        if (!isOwner) {
            List<FundMember> members = fundMemberRepository.findByShareFundId(fund.getId());
            for (FundMember member : members) {
                if (member.getUser().getId().equals(userId)
                        && FundRole.OWNER.name().equals(member.getFundRole())) {
                    isOwner = true;
                    break;
                }
            }
        }

        if (!isOwner) {
            throw new IllegalArgumentException("Chỉ Chủ quỹ (Owner) mới có quyền phê duyệt hoặc từ chối giao dịch!");
        }

        if (!"PENDING".equalsIgnoreCase(transaction.getStatus())) {
            throw new IllegalStateException("Giao dịch này đã được xử lý trước đó! Trạng thái: " + transaction.getStatus());
        }

        User approver = userRepository.findById(userId).orElse(null);

        if ("approved".equalsIgnoreCase(action)) {
            transaction.setIsApproved(true);
            transaction.setStatus("APPROVED");
            transaction.setApprovedByUser(approver);
            transaction.setApprovedAt(LocalDateTime.now());

            BigDecimal currentBalance = fund.getBalance();
            TransactionType txType = TransactionType.valueOf(transaction.getType());

            if (TransactionType.INCOME == txType) {
                fund.setBalance(currentBalance.add(transaction.getAmount()));
                log.info("INCOME APPROVED: balance +{} -> new balance={}", transaction.getAmount(), fund.getBalance());
            } else if (TransactionType.EXPENSE == txType) {
                fund.setBalance(currentBalance.subtract(transaction.getAmount()));
                log.info("EXPENSE APPROVED: balance -{} -> new balance={}", transaction.getAmount(), fund.getBalance());
            }
            applyPersonalFundImpact(transaction);
            shareFundRepository.save(fund);

            String type = "INCOME".equalsIgnoreCase(transaction.getType()) ? "DEPOSIT_APPROVED" : "WITHDRAW_APPROVED";
            String description = ("INCOME".equalsIgnoreCase(transaction.getType()) ? "Yêu cầu nạp tiền vào quỹ " : "Yêu cầu rút tiền từ quỹ ") + fund.getName() + " đã được phê duyệt.";
            try {
                notificationService.createAndSendNotification(
                        transaction.getUser(),
                        type,
                        fund.getId(),
                        fund.getName(),
                        transaction.getAmount(),
                        description,
                        approver != null ? (approver.getDisplayName() != null ? approver.getDisplayName() : approver.getEmail()) : "Chủ quỹ",
                        null,
                        null,
                        "MEMBER",
                        "/shared-funds"
                );
            } catch (Exception e) {
                log.error("Failed to send transaction approved notification", e);
            }
        } else if ("rejected".equalsIgnoreCase(action)) {
            transaction.setIsApproved(false);
            transaction.setStatus("REJECTED");
            transaction.setRejectReason(rejectReason);
            sendTransactionRejectedNotification(transaction, fund, rejectReason);
            log.info("TRANSACTION REJECTED: transactionId={}", transactionId);
        } else {
            throw new IllegalArgumentException("Hành động không hợp lệ! Chỉ chấp nhận approved hoặc rejected.");
        }

        return transactionRepository.save(transaction);
    }

    private PersonalFund findActiveUserPersonalFund(Long personalFundId, Long userId) {
        PersonalFund personalFund = personalFundRepository.findByIdAndUserId(personalFundId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguồn tiền với ID: " + personalFundId));
        if (personalFund.getStatus() != FundStatus.ACTIVE) {
            throw new IllegalArgumentException("Nguồn tiền '" + personalFund.getName() + "' đã bị đóng!");
        }
        return personalFund;
    }

    private void validateFundCategory(Category category, String transactionType, Long userId) {
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền dùng danh mục này!");
        }
        if (!category.getType().equalsIgnoreCase(transactionType)) {
            throw new IllegalArgumentException("Danh mục không khớp với loại giao dịch quỹ!");
        }
    }

    private void applyPersonalFundImpact(Transaction transaction) {
        PersonalFund personalFund = transaction.getPersonalFund();
        if (personalFund == null) {
            return;
        }

        TransactionType txType = TransactionType.valueOf(transaction.getType());
        BigDecimal amount = transaction.getAmount();
        if (TransactionType.INCOME == txType) {
            if (personalFund.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Số dư nguồn tiền '" + personalFund.getName() + "' không đủ!");
            }
            personalFund.setBalance(personalFund.getBalance().subtract(amount));
        } else if (TransactionType.EXPENSE == txType) {
            personalFund.setBalance(personalFund.getBalance().add(amount));
        }
        personalFundRepository.save(personalFund);
    }

    private void sendTransactionRejectedNotification(Transaction transaction, ShareFund fund, String rejectReason) {
        User requester = transaction.getUser();
        if (requester == null) {
            return;
        }

        String type = "INCOME".equalsIgnoreCase(transaction.getType())
                ? "DEPOSIT_REJECTED"
                : "WITHDRAW_REJECTED";
        String reason = rejectReason != null && !rejectReason.isBlank()
                ? rejectReason.trim()
                : "Chu quy khong neu ly do";
        String description = "Ly do tu choi: " + reason;

        try {
            notificationService.createAndSendNotification(
                    requester,
                    type,
                    fund.getId(),
                    fund.getName(),
                    transaction.getAmount(),
                    description,
                    fund.getCreatedByUser() != null ? fund.getCreatedByUser().getDisplayName() : "Chủ quỹ",
                    null,
                    null,
                    "MEMBER",
                    "/shared-funds"
            );
        } catch (Exception e) {
            log.error("Failed to send transaction rejected notification", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShareFund> getFundsForUser(Long userId) {
        List<FundMember> memberships = fundMemberRepository.findByUserId(userId);
        List<ShareFund> funds = new java.util.ArrayList<>();
        for (FundMember member : memberships) {
            if (member.getShareFund() != null) {
                ShareFund fund = member.getShareFund();
                if (fund.getMembers() != null) {
                    fund.getMembers().size();
                    for (FundMember fm : fund.getMembers()) {
                        if (fm.getUser() != null) {
                            fm.getUser().getDisplayName();
                        }
                    }
                }
                funds.add(fund);
            }
        }
        return funds;
    }

    @Override
    @Transactional
    public ShareFund createFund(String name, java.math.BigDecimal target, java.math.BigDecimal initialContribution, Long userId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên quỹ không được để trống!");
        }
        String trimmedName = name.trim();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        java.util.List<ShareFund> existingFunds = shareFundRepository.findByCreatedByUserId(userId);
        for (ShareFund existing : existingFunds) {
            if (existing.getName().trim().equalsIgnoreCase(trimmedName)) {
                throw new IllegalArgumentException("Bạn đã tạo một quỹ nhóm với tên này rồi! Vui lòng chọn tên khác.");
            }
        }

        ShareFund fund = ShareFund.builder()
                .name(trimmedName)
                .balance(initialContribution != null ? initialContribution : java.math.BigDecimal.ZERO)
                .status("ACTIVE")
                .createdByUser(user)
                .build();

        ShareFund savedFund = shareFundRepository.save(fund);

        FundMember ownerMember = FundMember.builder()
                .shareFund(savedFund)
                .user(user)
                .fundRole(FundRole.OWNER.name())
                .build();

        fundMemberRepository.save(ownerMember);

        BigDecimal initialAmount = initialContribution != null ? initialContribution : BigDecimal.ZERO;
        if (initialAmount.compareTo(BigDecimal.ZERO) > 0) {
            Transaction initialTransaction = Transaction.builder()
                    .user(user)
                    .shareFund(savedFund)
                    .amount(initialAmount)
                    .type(TransactionType.INCOME.name())
                    .description("Đóng góp ban đầu")
                    .date(java.time.LocalDate.now())
                    .isApproved(true)
                    .status("APPROVED")
                    .approvedByUser(user)
                    .approvedAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(initialTransaction);
        }

        return savedFund;
    }

    @Override
    @Transactional
    public ShareFund renameFund(Long fundId, String newName, Long userId) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên quỹ không được để trống!");
        }
        String trimmedName = newName.trim();

        ShareFund fund = shareFundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quỹ!"));

        boolean isOwner = fund.getCreatedByUser().getId().equals(userId);
        if (!isOwner) {
            FundMember member = fundMemberRepository.findByShareFundIdAndUserId(fundId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Bạn không phải thành viên quỹ!"));
            if (!FundRole.OWNER.name().equals(member.getFundRole())) {
                throw new IllegalArgumentException("Chỉ chủ quỹ mới có quyền đổi tên!");
            }
        }

        Long creatorId = fund.getCreatedByUser().getId();
        java.util.List<ShareFund> existingFunds = shareFundRepository.findByCreatedByUserId(creatorId);
        for (ShareFund existing : existingFunds) {
            if (!existing.getId().equals(fundId) && existing.getName().trim().equalsIgnoreCase(trimmedName)) {
                throw new IllegalArgumentException("Chủ quỹ đã có một quỹ nhóm khác với tên này! Vui lòng chọn tên khác.");
            }
        }

        fund.setName(trimmedName);
        return shareFundRepository.save(fund);
    }

    @Override
    @Transactional
    public void leaveFund(Long fundId, Long userId) {
        ShareFund fund = shareFundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quỹ!"));

        if (fund.getCreatedByUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Chủ quỹ sáng lập không thể rời quỹ! Bạn phải xóa quỹ.");
        }

        FundMember member = fundMemberRepository.findByShareFundIdAndUserId(fundId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không phải thành viên quỹ này!"));

        fundMemberRepository.delete(member);
    }

    @Override
    @Transactional
    public void removeMember(Long fundId, String memberEmail, Long ownerUserId) {
        ShareFund fund = shareFundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quỹ!"));

        boolean isOwner = fund.getCreatedByUser().getId().equals(ownerUserId);
        if (!isOwner) {
            FundMember member = fundMemberRepository.findByShareFundIdAndUserId(fundId, ownerUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Bạn không phải thành viên quỹ!"));
            if (!FundRole.OWNER.name().equals(member.getFundRole())) {
                throw new IllegalArgumentException("Chỉ chủ quỹ mới có quyền xóa thành viên!");
            }
        }

        User targetUser = userRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên với email này!"));

        if (fund.getCreatedByUser().getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("Không thể xóa chủ quỹ sáng lập!");
        }

        FundMember targetMember = fundMemberRepository.findByShareFundIdAndUserId(fundId, targetUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Người dùng này không thuộc quỹ!"));

        fundMemberRepository.delete(targetMember);

        try {
            notificationService.createAndSendNotification(
                    targetUser,
                    "FUND_MEMBER_REMOVED",
                    fund.getId(),
                    fund.getName(),
                    null,
                    "Bạn đã bị xóa khỏi quỹ nhóm " + fund.getName() + " bởi chủ quỹ.",
                    fund.getCreatedByUser() != null ? fund.getCreatedByUser().getDisplayName() : "Chủ quỹ",
                    null,
                    null,
                    "MEMBER",
                    "/shared-funds"
            );
        } catch (Exception e) {
            log.error("Failed to send member removed notification", e);
        }
    }

    private void cleanupFundData(Long fundId) {
        java.util.List<FundMessage> messages = fundMessageRepository.findByShareFundIdOrderByCreatedAtAsc(fundId);
        fundMessageRepository.deleteAllInBatch(messages);

        java.util.List<FundActivity> activities = fundActivityRepository.findByShareFundId(fundId);
        fundActivityRepository.deleteAllInBatch(activities);

        java.util.List<Transaction> transactions = transactionRepository.findByShareFundId(fundId);
        transactionRepository.deleteAllInBatch(transactions);
    }

    @Override
    @Transactional
    public String deleteFund(Long fundId, Long userId) {
        ShareFund fund = shareFundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quỹ!"));

        if (!fund.getCreatedByUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Chỉ người tạo quỹ mới được quyền xóa quỹ!");
        }

        String fundName = fund.getName();
        User owner = fund.getCreatedByUser();
        String ownerName = owner.getDisplayName() != null ? owner.getDisplayName() : owner.getEmail();
        final String ownerEmail = owner.getEmail();

        List<FundMember> members = fundMemberRepository.findByShareFundId(fundId);
        java.util.List<java.util.Map<String, String>> membersToNotify = new java.util.ArrayList<>();
        for (FundMember member : members) {
            User memberUser = member.getUser();
            if (memberUser != null && !memberUser.getId().equals(userId)) {
                java.util.Map<String, String> info = new java.util.HashMap<>();
                info.put("email", memberUser.getEmail());
                info.put("name", memberUser.getDisplayName() != null ? memberUser.getDisplayName() : memberUser.getEmail());
                membersToNotify.add(info);
            }
        }

        if (membersToNotify.isEmpty()) {
            cleanupFundData(fundId);
            shareFundRepository.delete(fund);
            return "Quỹ nhóm đã được giải tán thành công!";
        }

        List<FundInvitation> existingDisbandProposals = fundInvitationRepository.findByShareFundId(fundId)
                .stream()
                .filter(inv -> "DISBAND_PROPOSAL".equals(inv.getType()))
                .collect(java.util.stream.Collectors.toList());
        for (FundInvitation oldInv : existingDisbandProposals) {
            if ("PENDING".equals(oldInv.getStatus())) {
                oldInv.setStatus("CANCELLED");
                fundInvitationRepository.save(oldInv);
            }
        }

        for (FundMember member : members) {
            User memberUser = member.getUser();
            if (memberUser != null && !memberUser.getId().equals(userId)) {
                String email = memberUser.getEmail();
                String name = memberUser.getDisplayName() != null ? memberUser.getDisplayName() : memberUser.getEmail();

                FundInvitation invitation = FundInvitation.builder()
                        .shareFund(fund)
                        .invitedEmail(email)
                        .invitationToken(java.util.UUID.randomUUID().toString())
                        .status("PENDING")
                        .type("DISBAND_PROPOSAL")
                        .expiresAt(LocalDateTime.now().plusHours(12))
                        .build();

                fundInvitationRepository.save(invitation);

                try {
                    notificationService.createAndSendNotification(
                            memberUser,
                            "FUND_DISBAND_PROPOSAL",
                            fund.getId(),
                            fund.getName(),
                            null,
                            "Đề xuất giải tán quỹ nhóm " + fundName + " từ chủ quỹ " + ownerName,
                            ownerName,
                            null,
                            null,
                            "MEMBER",
                            "/shared-funds"
                    );
                } catch (Exception e) {
                    log.error("Failed to send disband proposal notification", e);
                }

                String tokenVal = invitation.getInvitationToken();
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        mailService.sendDisbandProposalEmail(email, name, fundName, ownerName, ownerEmail, "Yêu cầu giải tán quỹ nhóm từ chủ quỹ", tokenVal);
                        log.info("Đã gửi email đề xuất giải tán quỹ {} đến {}", fundName, email);
                    } catch (Exception e) {
                        log.error("Lỗi khi gửi email giải tán quỹ đến {}: {}", email, e.getMessage());
                    }
                });
            }
        }

        return "Đã gửi yêu cầu giải tán quỹ nhóm đến tất cả thành viên. Quỹ sẽ được xóa sau khi tất cả thành viên xác nhận đồng ý.";
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getFundTransactions(Long fundId, Long userId) {
        ShareFund fund = shareFundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quỹ!"));

        boolean isMember = fund.getCreatedByUser().getId().equals(userId);
        if (!isMember) {
            isMember = fundMemberRepository.existsByShareFundIdAndUserId(fundId, userId);
        }

        if (!isMember) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập giao dịch của quỹ này!");
        }

        return transactionRepository.findByShareFundIdWithDetails(fundId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getActivitiesForUser(Long userId) {
        List<ShareFund> userFunds = getFundsForUser(userId);
        List<java.util.Map<String, Object>> activities = new java.util.ArrayList<>();

        User user = userRepository.findById(userId).orElse(null);
        String userEmail = user != null ? user.getEmail() : "";

        long idCounter = 1;
        for (ShareFund fund : userFunds) {
            java.util.Map<String, Object> actCreate = new java.util.HashMap<>();
            actCreate.put("id", idCounter++);
            actCreate.put("email", userEmail);
            actCreate.put("type", "create");
            actCreate.put("text", "Bạn đã tham gia nhóm '" + fund.getName() + "'");
            actCreate.put("time", "Mới đây");
            actCreate.put("color", "#1A73E8");
            activities.add(actCreate);

            List<Transaction> transactions = transactionRepository.findByShareFundId(fund.getId());
            for (Transaction tx : transactions) {
                java.util.Map<String, Object> actTx = new java.util.HashMap<>();
                actTx.put("id", idCounter++);
                actTx.put("email", userEmail);
                
                String requesterName = tx.getUser().getDisplayName();
                String typeStr = "INCOME".equalsIgnoreCase(tx.getType()) ? "đóng góp" : "rút tiền";
                String actionColor = "INCOME".equalsIgnoreCase(tx.getType()) ? "#34A853" : "#EA4335";
                
                actTx.put("type", "INCOME".equalsIgnoreCase(tx.getType()) ? "join" : "leave");
                actTx.put("text", requesterName + " đã " + typeStr + " " + tx.getAmount() + "đ (" + (tx.getIsApproved() ? "Đã duyệt" : "Chờ duyệt") + ") vào quỹ '" + fund.getName() + "'");
                actTx.put("time", tx.getDate().toString());
                actTx.put("color", actionColor);
                activities.add(actTx);
            }
        }
        
        activities.sort((a, b) -> Long.compare((Long) b.get("id"), (Long) a.get("id")));
        return activities;
    }

    private String formatTime(java.time.LocalDateTime time) {
        if (time == null) return "";
        return time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM"));
    }

    private String defaultAvatar(User user) {
        if (user == null || user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            return "";
        }
        return user.getDisplayName().trim().substring(0, 1).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse> getDiscussions(Long fundId, Long userId) {
        ShareFund fund = shareFundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quỹ với ID: " + fundId));
        
        boolean isMember = fund.getCreatedByUser().getId().equals(userId);
        if (!isMember) {
            isMember = fundMemberRepository.existsByShareFundIdAndUserId(fundId, userId);
        }
        if (!isMember) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập thảo luận của quỹ này!");
        }

        return fundMessageRepository.findByShareFundIdOrderByCreatedAtAsc(fundId).stream()
                .map(msg -> {
                    User sender = msg.getSender();
                    String senderName = sender == null ? "System" : sender.getDisplayName();
                    return com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse.builder()
                            .id(msg.getId())
                            .groupId(fundId)
                            .senderName(senderName)
                            .senderAvatar(sender == null ? "" : defaultAvatar(sender))
                            .type(msg.getType())
                            .text(msg.getText())
                            .time(formatTime(msg.getCreatedAt()))
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse sendChatMessage(Long fundId, com.smartfinance.smart_finance_hub.dto.request.FundChatMessageRequest request, Long userId) {
        ShareFund fund = shareFundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quỹ với ID: " + fundId));
        
        boolean isMember = fund.getCreatedByUser().getId().equals(userId);
        if (!isMember) {
            isMember = fundMemberRepository.existsByShareFundIdAndUserId(fundId, userId);
        }
        if (!isMember) {
            throw new IllegalArgumentException("Bạn không phải thành viên của quỹ này!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống!");
        }

        FundMessage message = FundMessage.builder()
                .shareFund(fund)
                .sender(user)
                .type("user")
                .text(request.getContent().trim())
                .build();
        
        FundMessage saved = fundMessageRepository.save(message);
        
        return com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse.builder()
                .id(saved.getId())
                .groupId(fundId)
                .senderName(user.getDisplayName())
                .senderAvatar(defaultAvatar(user))
                .type(saved.getType())
                .text(saved.getText())
                .time(formatTime(saved.getCreatedAt()))
                .build();
    }

    @Override
    @Transactional
    public String verifyInvitationToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã xác nhận không hợp lệ!");
        }

        FundInvitation invitation = fundInvitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lời mời với mã xác nhận đã cho!"));

        if (!InvitationStatus.PENDING.name().equals(invitation.getStatus())) {
            throw new IllegalStateException("Lời mời này đã được xử lý trước đó! Trạng thái: " + invitation.getStatus());
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED.name());
            fundInvitationRepository.save(invitation);
            throw new IllegalStateException("Lời mời đã hết hạn!");
        }

        User respondUser = userRepository.findByEmail(invitation.getInvitedEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng được mời!"));

        if ("MEMBER_INVITE".equals(invitation.getType())) {
            invitation.setStatus(InvitationStatus.ACCEPTED.name());
            fundInvitationRepository.save(invitation);

            boolean alreadyMember = fundMemberRepository.existsByShareFundIdAndUserId(
                    invitation.getShareFund().getId(), respondUser.getId());
            if (!alreadyMember) {
                FundMember newMember = FundMember.builder()
                        .shareFund(invitation.getShareFund())
                        .user(respondUser)
                        .fundRole(FundRole.MEMBER.name())
                        .build();
                fundMemberRepository.save(newMember);
            }
            return "Chúc mừng! Bạn đã tham gia quỹ nhóm thành công.";
        } else if ("DISBAND_PROPOSAL".equals(invitation.getType())) {
            ShareFund fund = invitation.getShareFund();
            String fundName = fund.getName();

            List<FundMember> currentMembers = fundMemberRepository.findByShareFundId(fund.getId());
            java.util.List<String> otherMemberEmails = new java.util.ArrayList<>();
            java.util.List<java.util.Map<String, String>> membersToNotify = new java.util.ArrayList<>();
            
            for (FundMember m : currentMembers) {
                User memberUser = m.getUser();
                if (memberUser != null) {
                    java.util.Map<String, String> info = new java.util.HashMap<>();
                    info.put("email", memberUser.getEmail());
                    info.put("name", memberUser.getDisplayName() != null ? memberUser.getDisplayName() : memberUser.getEmail());
                    membersToNotify.add(info);
                    
                    if (!memberUser.getId().equals(fund.getCreatedByUser().getId())) {
                        otherMemberEmails.add(memberUser.getEmail().toLowerCase());
                    }
                }
            }

            List<FundInvitation> disbandInvitations = fundInvitationRepository.findByShareFundId(fund.getId())
                    .stream()
                    .filter(inv -> "DISBAND_PROPOSAL".equals(inv.getType())
                            && !"CANCELLED".equals(inv.getStatus())
                            && !"EXPIRED".equals(inv.getStatus()))
                    .collect(java.util.stream.Collectors.toList());

            boolean allAccepted = true;
            for (String email : otherMemberEmails) {
                boolean accepted = false;
                for (FundInvitation inv : disbandInvitations) {
                    if (inv.getInvitedEmail().equalsIgnoreCase(email)) {
                        if (inv.getId().equals(invitation.getId())) {
                            accepted = true;
                        } else if ("ACCEPTED".equals(inv.getStatus())) {
                            accepted = true;
                        }
                    }
                }
                if (!accepted) {
                    allAccepted = false;
                    break;
                }
            }

            if (allAccepted) {
                cleanupFundData(fund.getId());
                shareFundRepository.delete(fund);

                java.util.List<java.util.Map<String, String>> finalMembersToNotify = membersToNotify;
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    for (java.util.Map<String, String> memberInfo : finalMembersToNotify) {
                        String email = memberInfo.get("email");
                        String name = memberInfo.get("name");
                        try {
                            mailService.sendDisbandConfirmationEmail(email, name, fundName);
                            log.info("Đã gửi email thông báo giải tán quỹ {} đến {}", fundName, email);
                        } catch (Exception e) {
                            log.error("Lỗi khi gửi email giải tán quỹ đến {}: {}", email, e.getMessage());
                        }
                    }
                });
                return "Tất cả thành viên đã đồng ý giải tán. Quỹ nhóm \"" + fundName + "\" đã được giải tán thành công!";
            } else {
                invitation.setStatus(InvitationStatus.ACCEPTED.name());
                fundInvitationRepository.save(invitation);
                return "Bạn đã xác nhận đồng ý giải tán quỹ nhóm \"" + fundName + "\". Đang chờ phản hồi của các thành viên khác.";
            }
        } else {
            throw new IllegalArgumentException("Loại mã xác thực không hợp lệ!");
        }
    }
}
