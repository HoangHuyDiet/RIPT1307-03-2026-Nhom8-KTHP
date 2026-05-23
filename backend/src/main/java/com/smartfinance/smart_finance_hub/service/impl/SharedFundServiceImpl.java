package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.InviteMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondInvitationRequest;
import com.smartfinance.smart_finance_hub.entity.*;
import com.smartfinance.smart_finance_hub.enums.FundRole;
import com.smartfinance.smart_finance_hub.enums.InvitationStatus;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.*;
import com.smartfinance.smart_finance_hub.service.SharedFundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                throw new IllegalStateException(
                        "Đã có lời mời đang chờ xử lý cho email: " + request.getEmail());
            }
        }

        FundInvitation invitation = FundInvitation.builder()
                .shareFund(fund)
                .invitedEmail(request.getEmail())
                .invitationToken(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING.name())
                .type("MEMBER_INVITE")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        FundInvitation saved = fundInvitationRepository.save(invitation);
        log.info("inviteMember success: invitationId={}", saved.getId());
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

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .shareFund(fund)
                .amount(request.getAmount())
                .type(transactionType.name())
                .description(request.getDescription())
                .date(request.getDate())
                .isApproved(false)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("createFundTransaction success: transactionId={}, isApproved=false", saved.getId());
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

        shareFundRepository.save(fund);

        log.info("approveTransaction success: transactionId={}, isApproved=true, newBalance={}", transactionId, fund.getBalance());
        return transaction;
    }
}
