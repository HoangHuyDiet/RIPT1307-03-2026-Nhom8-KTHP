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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        ShareFund fund = ShareFund.builder()
                .name(name)
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
        return savedFund;
    }

    @Override
    @Transactional
    public ShareFund renameFund(Long fundId, String newName, Long userId) {
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

        fund.setName(newName);
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
    }

    @Override
    @Transactional
    public void deleteFund(Long fundId, Long userId) {
        ShareFund fund = shareFundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quỹ!"));

        if (!fund.getCreatedByUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Chỉ người tạo quỹ mới được quyền xóa quỹ!");
        }

        shareFundRepository.delete(fund);
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

        return transactionRepository.findByShareFundId(fundId);
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
}
