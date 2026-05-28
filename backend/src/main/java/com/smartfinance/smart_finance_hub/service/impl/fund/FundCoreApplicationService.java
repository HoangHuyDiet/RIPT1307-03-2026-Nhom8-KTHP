package com.smartfinance.smart_finance_hub.service.impl.fund;

import com.smartfinance.smart_finance_hub.dto.request.CreateFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateFundRequest;
import com.smartfinance.smart_finance_hub.dto.response.FeFundActivityResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundListResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundStatResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundResponse;
import com.smartfinance.smart_finance_hub.entity.Fund;
import com.smartfinance.smart_finance_hub.entity.FundMember;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.FundRole;
import com.smartfinance.smart_finance_hub.enums.FundStatus;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.FundMemberRepository;
import com.smartfinance.smart_finance_hub.repository.FundRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundCoreApplicationService {

    private final FundRepository fundRepository;
    private final FundMemberRepository fundMemberRepository;
    private final TransactionRepository transactionRepository;
    private final FundAccessService access;
    private final FundMapper mapper;
    private final FundNotificationService notifications;
    private final FundCategoryService fundCategoryService;

    @Transactional
    public FundResponse createFund(CreateFundRequest request, Long userId) {
        log.info("createFund: userId={}, name={}", userId, request.getName());
        User user = access.requireUser(userId);

        Fund fund = Fund.builder()
                .name(access.normalizeRequired(request.getName(), "Fund name is required"))
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .dueDate(request.getDueDate())
                .createdByUser(user)
                .status(FundStatus.ACTIVE.name())
                .build();

        Fund savedFund = fundRepository.save(fund);
        savedFund.setThemeColor(mapper.themeColor(savedFund.getId()));

        FundMember ownerMember = FundMember.builder()
                .fund(savedFund)
                .user(user)
                .fundRole(FundRole.OWNER.name())
                .build();
        fundMemberRepository.save(ownerMember);
        notifications.saveSystemMessage(savedFund, user.getDisplayName() + " created fund " + savedFund.getName());

        if (request.getInitialContribution() != null
                && request.getInitialContribution().compareTo(BigDecimal.ZERO) > 0) {
            Transaction initialTransaction = Transaction.builder()
                    .user(user)
                    .category(fundCategoryService.createFundCategory(
                            user, TransactionType.INCOME, "Initial contribution"))
                    .fund(savedFund)
                    .amount(request.getInitialContribution())
                    .type(TransactionType.INCOME.name())
                    .description("Initial contribution")
                    .date(LocalDate.now())
                    .isApproved(true)
                    .status("APPROVED")
                    .build();
            transactionRepository.save(initialTransaction);
            savedFund.setBalance(savedFund.getBalance().add(request.getInitialContribution()));
            notifications.saveSystemMessage(savedFund, user.getDisplayName() + " contributed "
                    + request.getInitialContribution() + " to fund " + savedFund.getName());
        }

        Fund persisted = fundRepository.save(savedFund);
        log.info("createFund success: fundId={}", persisted.getId());
        return FundResponse.from(persisted, 1, FundRole.OWNER.name());
    }

    @Transactional(readOnly = true)
    public FundResponse getFundById(Long fundId, Long userId) {
        Fund fund = access.requireFund(fundId);
        String myRole = access.getMyRole(fundId, userId);
        if (myRole == null) {
            throw new IllegalArgumentException("You are not a member of this fund");
        }
        return FundResponse.from(fund, access.getMemberCount(fundId), myRole);
    }

    @Transactional(readOnly = true)
    public List<FundResponse> getMyFunds(Long userId) {
        return fundMemberRepository.findByUserId(userId).stream()
                .map(member -> {
                    Fund fund = member.getFund();
                    return FundResponse.from(fund, access.getMemberCount(fund.getId()), member.getFundRole());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeFundListResponse> getFrontendFundList(Long userId) {
        return fundMemberRepository.findByUserId(userId).stream()
                .map(member -> mapper.toFeFundListResponse(member.getFund()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeFundStatResponse> getFrontendFundStats(Long userId) {
        List<Fund> funds = fundMemberRepository.findByUserId(userId).stream()
                .map(FundMember::getFund)
                .collect(Collectors.toList());

        BigDecimal totalBalance = funds.stream()
                .map(Fund::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long completedFunds = funds.stream()
                .filter(fund -> fund.getTargetAmount() != null
                        && fund.getBalance().compareTo(fund.getTargetAmount()) >= 0)
                .count();
        List<Long> fundIds = funds.stream().map(Fund::getId).collect(Collectors.toList());
        BigDecimal monthlyExpense = fundIds.isEmpty()
                ? BigDecimal.ZERO
                : transactionRepository.findByFundIdInAndIsApprovedAndDateBetween(
                        fundIds, true, LocalDate.now().withDayOfMonth(1), LocalDate.now()).stream()
                .filter(tx -> TransactionType.EXPENSE.name().equals(tx.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return List.of(
                FeFundStatResponse.builder()
                        .title("Tong quy tham gia")
                        .value(funds.size() + " Quy")
                        .trend("")
                        .icon("TeamOutlined")
                        .build(),
                FeFundStatResponse.builder()
                        .title("Tong so du")
                        .value(mapper.formatVnd(totalBalance))
                        .trend("")
                        .icon("WalletOutlined")
                        .build(),
                FeFundStatResponse.builder()
                        .title("Quy dat muc tieu")
                        .value(completedFunds + " Quy")
                        .trend("Thang nay")
                        .icon("TrophyOutlined")
                        .build(),
                FeFundStatResponse.builder()
                        .title("Tong chi tieu")
                        .value(mapper.formatVnd(monthlyExpense))
                        .trend("")
                        .icon("LineChartOutlined")
                        .build());
    }

    @Transactional(readOnly = true)
    public List<FeFundActivityResponse> getFrontendFundActivities(Long userId) {
        List<Long> fundIds = fundMemberRepository.findByUserId(userId).stream()
                .map(member -> member.getFund().getId())
                .collect(Collectors.toList());
        List<TimedActivity> activities = new ArrayList<>();
        long sequence = 1;
        for (Long fundId : fundIds) {
            Fund fund = access.requireFund(fundId);
            activities.add(new TimedActivity(fund.getCreatedAt(), FeFundActivityResponse.builder()
                    .id(sequence++)
                    .type("create")
                    .text("You joined fund '" + fund.getName() + "'")
                    .time(mapper.formatTime(fund.getCreatedAt()))
                    .color("#1890ff")
                    .build()));
            for (Transaction transaction : transactionRepository.findByFundIdAndIsApproved(fundId, true)) {
                boolean income = TransactionType.INCOME.name().equals(transaction.getType());
                activities.add(new TimedActivity(transaction.getCreatedAt(), FeFundActivityResponse.builder()
                        .id(sequence++)
                        .type(income ? "deposit" : "withdraw")
                        .text(transaction.getUser().getDisplayName() + (income ? " deposited " : " spent ")
                                + mapper.formatVnd(transaction.getAmount()) + " in fund " + fund.getName())
                        .time(mapper.formatTime(transaction.getCreatedAt()))
                        .color(income ? "#52c41a" : "#ff4d4f")
                        .build()));
            }
        }
        return activities.stream()
                .sorted((left, right) -> nullSafeTime(right.getOccurredAt()).compareTo(nullSafeTime(left.getOccurredAt())))
                .map(TimedActivity::getResponse)
                .limit(20)
                .collect(Collectors.toList());
    }

    @Transactional
    public FundResponse updateFund(Long fundId, UpdateFundRequest request, Long userId) {
        Fund fund = access.requireActiveFund(fundId);
        access.requireOwner(fundId, userId);

        if (request.getName() != null) {
            fund.setName(access.normalizeRequired(request.getName(), "Fund name cannot be blank"));
        }
        if (request.getDescription() != null) {
            fund.setDescription(request.getDescription());
        }
        if (request.getTargetAmount() != null) {
            fund.setTargetAmount(request.getTargetAmount());
        }
        if (request.getDueDate() != null) {
            fund.setDueDate(request.getDueDate());
        }

        Fund saved = fundRepository.save(fund);
        return FundResponse.from(saved, access.getMemberCount(fundId), FundRole.OWNER.name());
    }

    @Transactional
    public FundResponse renameFund(Long fundId, String newName, Long userId) {
        UpdateFundRequest request = new UpdateFundRequest();
        request.setName(newName);
        return updateFund(fundId, request, userId);
    }

    private LocalDateTime nullSafeTime(LocalDateTime time) {
        return time == null ? LocalDateTime.MIN : time;
    }

    private static class TimedActivity {
        private final LocalDateTime occurredAt;
        private final FeFundActivityResponse response;

        TimedActivity(LocalDateTime occurredAt, FeFundActivityResponse response) {
            this.occurredAt = occurredAt;
            this.response = response;
        }

        LocalDateTime getOccurredAt() {
            return occurredAt;
        }

        FeFundActivityResponse getResponse() {
            return response;
        }
    }
}
