package com.smartfinance.smart_finance_hub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartfinance.smart_finance_hub.dto.request.ApproveFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.CreateFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.InviteMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.KickMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondInvitationRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateFundRequest;
import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import com.smartfinance.smart_finance_hub.entity.FundMember;
import com.smartfinance.smart_finance_hub.entity.Fund;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.dto.response.FundStatisticsResponse;
import com.smartfinance.smart_finance_hub.dto.response.TopContributorResponse;
import com.smartfinance.smart_finance_hub.enums.FundInvitationType;
import com.smartfinance.smart_finance_hub.enums.FundRole;
import com.smartfinance.smart_finance_hub.enums.FundStatus;
import com.smartfinance.smart_finance_hub.enums.InvitationStatus;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.repository.FundInvitationRepository;
import com.smartfinance.smart_finance_hub.repository.FundMessageRepository;
import com.smartfinance.smart_finance_hub.repository.FundMemberRepository;
import com.smartfinance.smart_finance_hub.repository.FundRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.impl.FundServiceImpl;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundAccessService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundCategoryService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundChatApplicationService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundCoreApplicationService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundMapper;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundMemberApplicationService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundNotificationService;
import com.smartfinance.smart_finance_hub.service.impl.fund.FundTransactionApplicationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class FundServiceTest {

    private static final Long FUND_ID = 10L;
    private static final Long OWNER_ID = 1L;
    private static final Long MEMBER_ID = 2L;

    private FundServiceImpl fundService;

    @Mock
    private FundRepository fundRepository;

    @Mock
    private FundMemberRepository fundMemberRepository;

    @Mock
    private FundInvitationRepository fundInvitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FundMessageRepository fundMessageRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private MailService mailService;

    @Mock
    private FundNotificationService fundNotificationService;

    @BeforeEach
    void setUp() {
        FundAccessService accessService = new FundAccessService(
                fundRepository, fundMemberRepository, userRepository, fundInvitationRepository);
        FundMapper mapper = new FundMapper(fundMemberRepository, userRepository);
        FundCategoryService fundCategoryService = new FundCategoryService(categoryRepository);
        FundCoreApplicationService coreService = new FundCoreApplicationService(
                fundRepository,
                fundMemberRepository,
                transactionRepository,
                accessService,
                mapper,
                fundNotificationService,
                fundCategoryService);
        FundMemberApplicationService memberService = new FundMemberApplicationService(
                fundRepository,
                fundInvitationRepository,
                fundMemberRepository,
                userRepository,
                accessService,
                mapper,
                fundNotificationService);
        FundTransactionApplicationService transactionService = new FundTransactionApplicationService(
                fundRepository,
                fundMemberRepository,
                transactionRepository,
                categoryRepository,
                accessService,
                mapper,
                fundNotificationService,
                fundCategoryService);
        FundChatApplicationService chatService = new FundChatApplicationService(
                fundMessageRepository, accessService, mapper);
        fundService = new FundServiceImpl(coreService, memberService, transactionService, chatService);
    }

    @Test
    void createFund_shouldCreateFundAndOwnerMember() {
        User owner = user(OWNER_ID, "owner@example.com", "Owner");
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(fundRepository.save(any(Fund.class))).thenAnswer(invocation -> {
            Fund fund = invocation.getArgument(0);
            fund.setId(FUND_ID);
            return fund;
        });

        fundService.createFund(
                new CreateFundRequest("Trip", "Summer", new BigDecimal("1000"), null, null, null), OWNER_ID);

        ArgumentCaptor<FundMember> captor = ArgumentCaptor.forClass(FundMember.class);
        verify(fundMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getFundRole()).isEqualTo(FundRole.OWNER.name());
        assertThat(captor.getValue().getUser().getId()).isEqualTo(OWNER_ID);
    }

    @Test
    void updateFund_memberCannotUpdate() {
        Fund fund = fund();
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, MEMBER_ID))
                .thenReturn(Optional.of(member(MEMBER_ID, FundRole.MEMBER)));

        assertThatThrownBy(() -> fundService.updateFund(
                FUND_ID, new UpdateFundRequest("New", null, null, null), MEMBER_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void inviteMember_ownerSuccess() {
        Fund fund = fund();
        User owner = user(OWNER_ID, "owner@example.com", "Owner");
        User invited = user(MEMBER_ID, "member@example.com", "Member");
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, OWNER_ID))
                .thenReturn(Optional.of(member(OWNER_ID, FundRole.OWNER)));
        when(userRepository.findByEmail(invited.getEmail())).thenReturn(Optional.of(invited));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(fundMemberRepository.existsByFundIdAndUserId(FUND_ID, MEMBER_ID)).thenReturn(false);
        when(fundInvitationRepository.existsByFundIdAndInvitedEmailIgnoreCaseAndTypeAndStatus(
                eq(FUND_ID), eq(invited.getEmail()), eq(FundInvitationType.MEMBER_INVITE.name()),
                eq(InvitationStatus.PENDING.name()))).thenReturn(false);
        when(fundInvitationRepository.save(any(FundInvitation.class))).thenAnswer(invocation -> {
            FundInvitation invitation = invocation.getArgument(0);
            invitation.setId(99L);
            return invitation;
        });

        fundService.inviteMember(FUND_ID, new InviteMemberRequest(invited.getEmail()), OWNER_ID);

        ArgumentCaptor<FundInvitation> captor = ArgumentCaptor.forClass(FundInvitation.class);
        verify(fundInvitationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(FundInvitationType.MEMBER_INVITE.name());
    }

    @Test
    void inviteMember_duplicatePendingBlocked() {
        Fund fund = fund();
        User invited = user(MEMBER_ID, "member@example.com", "Member");
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, OWNER_ID))
                .thenReturn(Optional.of(member(OWNER_ID, FundRole.OWNER)));
        when(userRepository.findByEmail(invited.getEmail())).thenReturn(Optional.of(invited));
        when(fundMemberRepository.existsByFundIdAndUserId(FUND_ID, MEMBER_ID)).thenReturn(false);
        when(fundInvitationRepository.existsByFundIdAndInvitedEmailIgnoreCaseAndTypeAndStatus(
                anyLong(), anyString(), anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> fundService.inviteMember(
                FUND_ID, new InviteMemberRequest(invited.getEmail()), OWNER_ID))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void inviteMember_nonOwnerBlocked() {
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund()));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, MEMBER_ID))
                .thenReturn(Optional.of(member(MEMBER_ID, FundRole.MEMBER)));

        assertThatThrownBy(() -> fundService.inviteMember(
                FUND_ID, new InviteMemberRequest("user@example.com"), MEMBER_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void respondKick_acceptRemovesMember() {
        User memberUser = user(MEMBER_ID, "member@example.com", "Member");
        FundInvitation invitation = invitation(FundInvitationType.KICK_PROPOSAL, memberUser.getEmail());
        when(fundInvitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(memberUser));
        when(fundMemberRepository.existsByFundIdAndUserId(FUND_ID, MEMBER_ID)).thenReturn(true);
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, MEMBER_ID))
                .thenReturn(Optional.of(member(MEMBER_ID, FundRole.MEMBER)));
        when(fundInvitationRepository.findByFundIdAndInvitedEmailIgnoreCaseAndStatus(
                FUND_ID, memberUser.getEmail(), InvitationStatus.PENDING.name())).thenReturn(List.of());
        fundService.respondToInvitation(
                FUND_ID, new RespondInvitationRequest(1L, "ACCEPT"), MEMBER_ID);

        verify(fundMemberRepository).delete(any(FundMember.class));
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED.name());
    }

    @Test
    void respondKick_rejectKeepsMember() {
        User memberUser = user(MEMBER_ID, "member@example.com", "Member");
        FundInvitation invitation = invitation(FundInvitationType.KICK_PROPOSAL, memberUser.getEmail());
        when(fundInvitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(memberUser));
        when(fundMemberRepository.existsByFundIdAndUserId(FUND_ID, MEMBER_ID)).thenReturn(true);
        fundService.respondToInvitation(
                FUND_ID, new RespondInvitationRequest(1L, "REJECT"), MEMBER_ID);

        verify(fundMemberRepository, never()).delete(any(FundMember.class));
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.REJECTED.name());
    }

    @Test
    void kickMember_cannotKickSelf() {
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund()));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, OWNER_ID))
                .thenReturn(Optional.of(member(OWNER_ID, FundRole.OWNER)));
        when(fundMemberRepository.findByFundIdAndUserEmailIgnoreCase(FUND_ID, "owner@example.com"))
                .thenReturn(Optional.of(member(OWNER_ID, FundRole.OWNER)));

        assertThatThrownBy(() -> fundService.kickMember(
                FUND_ID, new KickMemberRequest("owner@example.com", "reason"), OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void leaveFund_memberSuccess() {
        Fund fund = fund();
        User memberUser = user(MEMBER_ID, "member@example.com", "Member");
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, MEMBER_ID))
                .thenReturn(Optional.of(member(MEMBER_ID, FundRole.MEMBER)));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(memberUser));
        when(fundInvitationRepository.findByFundIdAndInvitedEmailIgnoreCaseAndStatus(
                FUND_ID, memberUser.getEmail(), InvitationStatus.PENDING.name())).thenReturn(List.of());
        when(fundMemberRepository.findByFundId(FUND_ID))
                .thenReturn(List.of(member(OWNER_ID, FundRole.OWNER), member(MEMBER_ID, FundRole.MEMBER)));

        fundService.leaveFund(FUND_ID, MEMBER_ID);

        verify(fundMemberRepository).delete(any(FundMember.class));
    }

    @Test
    void leaveFund_ownerBlocked() {
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund()));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, OWNER_ID))
                .thenReturn(Optional.of(member(OWNER_ID, FundRole.OWNER)));

        assertThatThrownBy(() -> fundService.leaveFund(FUND_ID, OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void respondDisband_allAcceptDisbands() {
        User memberUser = user(MEMBER_ID, "member@example.com", "Member");
        Fund fund = fund();
        FundInvitation invitation = invitation(FundInvitationType.DISBAND_PROPOSAL, memberUser.getEmail());
        invitation.setFund(fund);
        when(fundInvitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(memberUser));
        when(fundInvitationRepository.findByFundIdAndTypeAndStatus(
                FUND_ID, FundInvitationType.DISBAND_PROPOSAL.name(), InvitationStatus.PENDING.name()))
                .thenReturn(List.of());
        when(fundMemberRepository.findByFundId(FUND_ID))
                .thenReturn(List.of(member(OWNER_ID, FundRole.OWNER), member(MEMBER_ID, FundRole.MEMBER)));

        fundService.respondToInvitation(
                FUND_ID, new RespondInvitationRequest(1L, "ACCEPT"), MEMBER_ID);

        assertThat(fund.getStatus()).isEqualTo(FundStatus.DISBANDED.name());
    }

    @Test
    void respondDisband_rejectCancelsRemaining() {
        User memberUser = user(MEMBER_ID, "member@example.com", "Member");
        FundInvitation invitation = invitation(FundInvitationType.DISBAND_PROPOSAL, memberUser.getEmail());
        FundInvitation remaining = invitation(FundInvitationType.DISBAND_PROPOSAL, "other@example.com");
        when(fundInvitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(memberUser));
        when(fundInvitationRepository.findByFundIdAndTypeAndStatus(
                FUND_ID, FundInvitationType.DISBAND_PROPOSAL.name(), InvitationStatus.PENDING.name()))
                .thenReturn(List.of(remaining));
        fundService.respondToInvitation(
                FUND_ID, new RespondInvitationRequest(1L, "REJECT"), MEMBER_ID);

        assertThat(remaining.getStatus()).isEqualTo(InvitationStatus.CANCELLED.name());
    }

    @Test
    void createTransaction_disbandedFundBlocked() {
        Fund fund = fund();
        fund.setStatus(FundStatus.DISBANDED.name());
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));

        assertThatThrownBy(() -> fundService.createFundTransaction(
                FUND_ID, new CreateFundTransactionRequest(
                        BigDecimal.TEN, TransactionType.INCOME.name(), null, LocalDate.now(), 1L), MEMBER_ID))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void approveTransaction_incomeIncreasesBalance() {
        Fund fund = fund();
        fund.setBalance(new BigDecimal("50"));
        Transaction transaction = transaction(TransactionType.INCOME, new BigDecimal("100"), fund, false);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, OWNER_ID))
                .thenReturn(Optional.of(member(OWNER_ID, FundRole.OWNER)));

        fundService.approveTransaction(1L, OWNER_ID);

        assertThat(fund.getBalance()).isEqualByComparingTo("150");
    }

    @Test
    void approveTransaction_duplicateBlocked() {
        Fund fund = fund();
        Transaction transaction = transaction(TransactionType.INCOME, BigDecimal.TEN, fund, true);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, OWNER_ID))
                .thenReturn(Optional.of(member(OWNER_ID, FundRole.OWNER)));

        assertThatThrownBy(() -> fundService.approveTransaction(1L, OWNER_ID))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void respondInvite_duplicateMemberCancelled() {
        User memberUser = user(MEMBER_ID, "member@example.com", "Member");
        FundInvitation invitation = invitation(FundInvitationType.MEMBER_INVITE, memberUser.getEmail());
        when(fundInvitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(memberUser));
        when(fundMemberRepository.existsByFundIdAndUserId(FUND_ID, MEMBER_ID)).thenReturn(true);

        assertThatThrownBy(() -> fundService.respondToInvitation(
                FUND_ID, new RespondInvitationRequest(1L, "ACCEPT"), MEMBER_ID))
                .isInstanceOf(IllegalStateException.class);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.CANCELLED.name());
        verify(fundMemberRepository, never()).save(any(FundMember.class));
    }

    @Test
    void approveOrRejectFrontendTransaction_rejectMarksTransactionRejected() {
        Fund fund = fund();
        Transaction transaction = transaction(TransactionType.EXPENSE, new BigDecimal("80"), fund, false);
        transaction.setStatus("PENDING");
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, OWNER_ID))
                .thenReturn(Optional.of(member(OWNER_ID, FundRole.OWNER)));

        fundService.approveOrRejectFrontendTransaction(
                new ApproveFundTransactionRequest(1L, "rejected"), OWNER_ID);

        assertThat(transaction.getStatus()).isEqualTo("REJECTED");
        assertThat(transaction.getIsApproved()).isFalse();
        assertThat(fund.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getFundStatistics_calculatesIncomeExpenseAndCounts() {
        Fund fund = fund();
        Transaction income = transaction(TransactionType.INCOME, new BigDecimal("100"), fund, true);
        Transaction expense = transaction(TransactionType.EXPENSE, new BigDecimal("30"), fund, true);
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, MEMBER_ID))
                .thenReturn(Optional.of(member(MEMBER_ID, FundRole.MEMBER)));
        when(fundMemberRepository.findByFundId(FUND_ID))
                .thenReturn(List.of(member(OWNER_ID, FundRole.OWNER), member(MEMBER_ID, FundRole.MEMBER)));
        when(transactionRepository.findByFundIdAndIsApproved(FUND_ID, true))
                .thenReturn(List.of(income, expense));
        when(transactionRepository.findByFundIdAndIsApprovedAndDateBetween(
                eq(FUND_ID), eq(true), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(income));
        when(transactionRepository.countByFundId(FUND_ID)).thenReturn(3L);
        when(transactionRepository.countByFundIdAndIsApproved(FUND_ID, false)).thenReturn(1L);

        FundStatisticsResponse response = fundService.getFundStatistics(FUND_ID, MEMBER_ID);

        assertThat(response.getTotalIncome()).isEqualByComparingTo("100");
        assertThat(response.getTotalExpense()).isEqualByComparingTo("30");
        assertThat(response.getTotalTransactions()).isEqualTo(3);
        assertThat(response.getApprovedTransactions()).isEqualTo(2);
        assertThat(response.getPendingTransactions()).isEqualTo(1);
    }

    @Test
    void getTopContributors_sortsDescendingAndCalculatesPercent() {
        Fund fund = fund();
        Transaction memberIncome = transaction(TransactionType.INCOME, new BigDecimal("100"), fund, true);
        Transaction ownerIncome = transaction(TransactionType.INCOME, new BigDecimal("50"), fund, true);
        ownerIncome.setUser(user(OWNER_ID, "owner@example.com", "Owner"));
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));
        when(fundMemberRepository.findByFundIdAndUserId(FUND_ID, MEMBER_ID))
                .thenReturn(Optional.of(member(MEMBER_ID, FundRole.MEMBER)));
        when(transactionRepository.findByFundIdAndIsApproved(FUND_ID, true))
                .thenReturn(List.of(ownerIncome, memberIncome));

        List<TopContributorResponse> response = fundService.getTopContributors(FUND_ID, MEMBER_ID);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getName()).isEqualTo("Member");
        assertThat(response.get(0).getAmount()).isEqualByComparingTo("100");
        assertThat(response.get(0).getPercent()).isEqualTo(67);
    }

    private Fund fund() {
        return Fund.builder()
                .id(FUND_ID)
                .name("Fund")
                .status(FundStatus.ACTIVE.name())
                .balance(BigDecimal.ZERO)
                .createdByUser(user(OWNER_ID, "owner@example.com", "Owner"))
                .build();
    }

    private FundMember member(Long userId, FundRole role) {
        return FundMember.builder()
                .id(userId + 100)
                .fund(fund())
                .user(user(userId, userId.equals(OWNER_ID) ? "owner@example.com" : "member@example.com",
                        userId.equals(OWNER_ID) ? "Owner" : "Member"))
                .fundRole(role.name())
                .build();
    }

    private User user(Long id, String email, String displayName) {
        return User.builder()
                .id(id)
                .email(email)
                .password("password")
                .displayName(displayName)
                .build();
    }

    private FundInvitation invitation(FundInvitationType type, String email) {
        return FundInvitation.builder()
                .id(1L)
                .fund(fund())
                .invitedEmail(email)
                .invitationToken("token")
                .type(type.name())
                .status(InvitationStatus.PENDING.name())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    private Transaction transaction(
            TransactionType type, BigDecimal amount, Fund fund, boolean approved) {
        return Transaction.builder()
                .id(1L)
                .user(user(MEMBER_ID, "member@example.com", "Member"))
                .category(Category.builder().id(1L).name("Category").type(type.name()).build())
                .fund(fund)
                .amount(amount)
                .type(type.name())
                .description("tx")
                .date(LocalDate.now())
                .isApproved(approved)
                .status(approved ? "APPROVED" : "PENDING")
                .build();
    }
}


