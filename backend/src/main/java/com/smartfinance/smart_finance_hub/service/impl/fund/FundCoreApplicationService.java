package com.smartfinance.smart_finance_hub.service.impl.fund;

import com.smartfinance.smart_finance_hub.dto.request.CreateFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateFundRequest;
import com.smartfinance.smart_finance_hub.dto.response.FeFundActivityResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundListResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundStatResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundResponse;
import com.smartfinance.smart_finance_hub.dto.response.PersonalFundDashboardResponse;
import com.smartfinance.smart_finance_hub.entity.Fund;
import com.smartfinance.smart_finance_hub.entity.FundMember;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.FundRole;
import com.smartfinance.smart_finance_hub.enums.FundStatus;
import com.smartfinance.smart_finance_hub.enums.FundType;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.FundMemberRepository;
import com.smartfinance.smart_finance_hub.repository.FundRepository;
import com.smartfinance.smart_finance_hub.repository.FundActivityRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final FundActivityRepository fundActivityRepository;
    private final TransactionRepository transactionRepository;
    private final FundAccessService access;
    private final FundMapper mapper;
    private final FundNotificationService notifications;
    private final FundCategoryService fundCategoryService;

    @Transactional
    public FundResponse createFund(CreateFundRequest request, Long userId) {
        log.info("createFund: userId={}, name={}", userId, request.getName());
        User user = access.requireUser(userId);

        String fundType = resolveFundType(request.getFundType());

        Fund fund = Fund.builder()
                .name(access.normalizeRequired(request.getName(), "Fund name is required"))
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .dueDate(request.getDueDate())
                .createdByUser(user)
                .status(FundStatus.ACTIVE.name())
                .fundType(fundType)
                .walletType(FundType.PERSONAL.name().equals(fundType) ? resolveWalletType(request.getWalletType()) : null)
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
        notifications.saveActivity(savedFund, user, "CREATE_FUND",
                "You created fund '" + savedFund.getName() + "'", "#1890ff");

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
            notifications.saveActivity(savedFund, user, "APPROVE_TX",
                    "Initial contribution " + mapper.formatVnd(request.getInitialContribution())
                            + " was added to " + savedFund.getName(),
                    "#52c41a");
        }

        Fund persisted = fundRepository.save(savedFund);
        log.info("createFund success: fundId={}, fundType={}", persisted.getId(), fundType);
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
        return getMyFunds(userId, null);
    }

    @Transactional(readOnly = true)
    public List<FundResponse> getMyFunds(Long userId, String fundTypeFilter) {
        return fundMemberRepository.findByUserId(userId).stream()
                .filter(member -> fundTypeFilter == null || fundTypeFilter.equalsIgnoreCase(member.getFund().getFundType()))
                .map(member -> {
                    Fund fund = member.getFund();
                    return FundResponse.from(fund, access.getMemberCount(fund.getId()), member.getFundRole());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeFundListResponse> getFrontendFundList(Long userId) {
        return getFrontendFundList(userId, null);
    }

    @Transactional(readOnly = true)
    public List<FeFundListResponse> getFrontendFundList(Long userId, String fundTypeFilter) {
        return fundMemberRepository.findByUserId(userId).stream()
                .filter(member -> fundTypeFilter == null || fundTypeFilter.equalsIgnoreCase(member.getFund().getFundType()))
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
                        .title("Total funds")
                        .value(funds.size() + " funds")
                        .trend("")
                        .icon("TeamOutlined")
                        .build(),
                FeFundStatResponse.builder()
                        .title("Total balance")
                        .value(mapper.formatVnd(totalBalance))
                        .trend("")
                        .icon("WalletOutlined")
                        .build(),
                FeFundStatResponse.builder()
                        .title("Completed funds")
                        .value(completedFunds + " funds")
                        .trend("This month")
                        .icon("TrophyOutlined")
                        .build(),
                FeFundStatResponse.builder()
                        .title("Monthly spending")
                        .value(mapper.formatVnd(monthlyExpense))
                        .trend("")
                        .icon("LineChartOutlined")
                        .build());
    }

    @Transactional(readOnly = true)
    public List<FeFundActivityResponse> getFrontendFundActivities(Long userId) {
        return fundActivityRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toActivityResponse)
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

    @Transactional
    public FundResponse closeFund(Long fundId, Long userId) {
        Fund fund = access.requireActiveFund(fundId);
        access.requireOwner(fundId, userId);

        if (!FundType.PERSONAL.name().equals(fund.getFundType())) {
            throw new IllegalStateException("Only personal funds can be closed. Use disband for group funds");
        }

        fund.setStatus(FundStatus.CLOSED.name());
        Fund saved = fundRepository.save(fund);
        notifications.saveActivity(fund, access.requireUser(userId), "CLOSE_FUND",
                "You closed fund '" + fund.getName() + "'", "#ff4d4f");
        log.info("closeFund: fundId={} closed by userId={}", fundId, userId);
        return FundResponse.from(saved, 1, FundRole.OWNER.name());
    }

    @Transactional(readOnly = true)
    public PersonalFundDashboardResponse getPersonalDashboard(Long userId) {
        List<Fund> personalFunds = fundMemberRepository.findByUserId(userId).stream()
                .map(FundMember::getFund)
                .filter(fund -> FundType.PERSONAL.name().equals(fund.getFundType()))
                .filter(fund -> FundStatus.ACTIVE.name().equals(fund.getStatus()))
                .collect(Collectors.toList());

        BigDecimal totalAssets = personalFunds.stream()
                .map(Fund::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PersonalFundDashboardResponse.FundAllocation> allocations = personalFunds.stream()
                .map(fund -> {
                    double percent = totalAssets.compareTo(BigDecimal.ZERO) == 0 ? 0
                            : fund.getBalance().doubleValue() / totalAssets.doubleValue() * 100;
                    return PersonalFundDashboardResponse.FundAllocation.builder()
                            .fundId(fund.getId())
                            .fundName(fund.getName())
                            .walletType(fund.getWalletType())
                            .balance(fund.getBalance())
                            .percent(Math.round(percent * 100.0) / 100.0)
                            .themeColor(mapper.resolveThemeColor(fund))
                            .build();
                })
                .collect(Collectors.toList());

        // Monthly income/expense this month
        List<Long> fundIds = personalFunds.stream().map(Fund::getId).collect(Collectors.toList());
        BigDecimal monthlyIncome = BigDecimal.ZERO;
        BigDecimal monthlyExpense = BigDecimal.ZERO;
        if (!fundIds.isEmpty()) {
            List<Transaction> monthlyTx = transactionRepository.findByFundIdInAndIsApprovedAndDateBetween(
                    fundIds, true, LocalDate.now().withDayOfMonth(1), LocalDate.now());
            for (Transaction tx : monthlyTx) {
                if (TransactionType.INCOME.name().equals(tx.getType())) {
                    monthlyIncome = monthlyIncome.add(tx.getAmount());
                } else if (TransactionType.EXPENSE.name().equals(tx.getType())) {
                    monthlyExpense = monthlyExpense.add(tx.getAmount());
                }
            }
        }

        // Balance trends (last 6 months)
        List<PersonalFundDashboardResponse.BalanceTrend> trends = new ArrayList<>();
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        for (int monthsAgo = 5; monthsAgo >= 0; monthsAgo--) {
            LocalDate monthStart = currentMonth.minusMonths(monthsAgo);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            BigDecimal monthBalance = totalAssets;
            if (!fundIds.isEmpty()) {
                for (Transaction tx : transactionRepository.findByFundIdInAndIsApprovedAndDateBetween(
                        fundIds, true, monthEnd.plusDays(1), LocalDate.now())) {
                    if (TransactionType.INCOME.name().equals(tx.getType())) {
                        monthBalance = monthBalance.subtract(tx.getAmount());
                    } else if (TransactionType.EXPENSE.name().equals(tx.getType())) {
                        monthBalance = monthBalance.add(tx.getAmount());
                    }
                }
            }
            trends.add(PersonalFundDashboardResponse.BalanceTrend.builder()
                    .month("T" + monthStart.getMonthValue())
                    .totalBalance(monthBalance.max(BigDecimal.ZERO))
                    .build());
        }

        return PersonalFundDashboardResponse.builder()
                .totalAssets(totalAssets)
                .totalFunds(personalFunds.size())
                .totalIncomeThisMonth(monthlyIncome)
                .totalExpenseThisMonth(monthlyExpense)
                .allocations(allocations)
                .balanceTrends(trends)
                .recentActivities(fundActivityRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream()
                        .filter(activity -> FundType.PERSONAL.name().equals(activity.getFund().getFundType()))
                        .map(mapper::toActivityResponse)
                        .limit(10)
                        .collect(Collectors.toList()))
                .build();
    }

    public boolean isPersonalFund(Fund fund) {
        return FundType.PERSONAL.name().equals(fund.getFundType());
    }

    private String resolveFundType(String fundType) {
        if (fundType == null || fundType.isBlank()) {
            return FundType.GROUP.name();
        }
        try {
            return FundType.valueOf(fundType.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            return FundType.GROUP.name();
        }
    }

    private String resolveWalletType(String walletType) {
        if (walletType == null || walletType.isBlank()) {
            return "OTHER";
        }
        return walletType.toUpperCase();
    }

}
