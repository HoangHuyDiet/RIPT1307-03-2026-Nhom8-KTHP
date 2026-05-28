package com.smartfinance.smart_finance_hub.service.impl.fund;

import com.smartfinance.smart_finance_hub.dto.request.ApproveFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.FundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.response.BudgetChartResponse;
import com.smartfinance.smart_finance_hub.dto.response.FeFundTransactionResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundStatisticsResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundTransactionResponse;
import com.smartfinance.smart_finance_hub.dto.response.TopContributorResponse;
import com.smartfinance.smart_finance_hub.entity.Fund;
import com.smartfinance.smart_finance_hub.entity.FundMember;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.repository.FundMemberRepository;
import com.smartfinance.smart_finance_hub.repository.FundRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundTransactionApplicationService {

    private final FundRepository fundRepository;
    private final FundMemberRepository fundMemberRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final FundAccessService access;
    private final FundMapper mapper;
    private final FundNotificationService notifications;
    private final FundCategoryService fundCategoryService;

    @Transactional
    public FundTransactionResponse createFundTransaction(
            Long fundId, CreateFundTransactionRequest request, Long userId) {
        Fund fund = access.requireActiveFund(fundId);
        access.requireMember(fundId, userId);
        User user = access.requireUser(userId);
        requirePositiveAmount(request.getAmount());

        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found: " + request.getCategoryId()));
        TransactionType transactionType = access.parseTransactionType(request.getType());

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .fund(fund)
                .amount(request.getAmount())
                .type(transactionType.name())
                .description(request.getDescription())
                .date(request.getDate())
                .isApproved(false)
                .status("PENDING")
                .build();

        Transaction saved = transactionRepository.save(transaction);
        notifications.notifyOwnerTransactionRequest(fund, saved);
        return FundTransactionResponse.from(saved);
    }

    @Transactional
    public FeFundTransactionResponse createFrontendTransactionRequest(
            FundTransactionRequest request, Long userId) {
        Fund fund = access.requireActiveFund(request.getFundId());
        access.requireMember(request.getFundId(), userId);
        User user = access.requireUser(userId);
        requirePositiveAmount(request.getAmount());
        TransactionType transactionType = access.parseTransactionType(request.getType());
        var category = fundCategoryService.createFundCategory(
                user, transactionType, transactionType == TransactionType.INCOME ? "Other income" : "Other expense");

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .fund(fund)
                .amount(request.getAmount())
                .type(transactionType.name())
                .description(request.getDescription())
                .date(LocalDate.now())
                .isApproved(false)
                .status("PENDING")
                .bankAccount(request.getBankAccount())
                .bankName(request.getBankName())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        notifications.notifyOwnerTransactionRequest(fund, saved);
        return mapper.toFeTransactionResponse(saved);
    }

    @Transactional
    public FundTransactionResponse approveTransaction(Long transactionId, Long approverUserId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        Fund fund = transaction.getFund();
        if (fund == null) {
            throw new IllegalArgumentException("Transaction does not belong to a fund");
        }
        access.requireActiveFund(fund.getId());
        access.requireOwner(fund.getId(), approverUserId);

        if (Boolean.TRUE.equals(transaction.getIsApproved())) {
            throw new IllegalStateException("Transaction was already approved");
        }

        transaction.setIsApproved(true);
        transaction.setStatus("APPROVED");
        transactionRepository.save(transaction);

        applyApprovedAmount(fund, transaction);
        fundRepository.save(fund);
        notifications.saveSystemMessage(fund, "Owner approved " + mapper.formatVnd(transaction.getAmount())
                + " request from " + transaction.getUser().getDisplayName());
        notifications.notifyRequesterTransactionResult(transaction, "approved");

        return FundTransactionResponse.from(transaction);
    }

    @Transactional
    public FeFundTransactionResponse approveOrRejectFrontendTransaction(
            ApproveFundTransactionRequest request, Long approverUserId) {
        Transaction transaction = transactionRepository.findById(request.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + request.getRequestId()));
        Fund fund = transaction.getFund();
        if (fund == null) {
            throw new IllegalArgumentException("Transaction does not belong to a fund");
        }
        access.requireActiveFund(fund.getId());
        access.requireOwner(fund.getId(), approverUserId);
        if (!"PENDING".equalsIgnoreCase(transaction.getStatus())) {
            throw new IllegalStateException("Transaction request was already processed");
        }

        String action = request.getAction().trim().toLowerCase();
        if ("approved".equals(action) || "approve".equals(action)) {
            approveTransaction(transaction.getId(), approverUserId);
        } else if ("rejected".equals(action) || "reject".equals(action)) {
            transaction.setStatus("REJECTED");
            transaction.setIsApproved(false);
            transactionRepository.save(transaction);
            notifications.saveSystemMessage(fund, "Owner rejected " + mapper.formatVnd(transaction.getAmount())
                    + " request from " + transaction.getUser().getDisplayName());
            notifications.notifyRequesterTransactionResult(transaction, "rejected");
        } else {
            throw new IllegalArgumentException("action must be approved or rejected");
        }
        return mapper.toFeTransactionResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<FundTransactionResponse> getFundTransactions(
            Long fundId, Long userId, String type, Pageable pageable) {
        access.requireFund(fundId);
        access.requireMember(fundId, userId);

        Page<Transaction> page;
        if (type != null && !type.isBlank()) {
            page = transactionRepository.findByFundIdAndType(
                    fundId, access.parseTransactionType(type).name(), pageable);
        } else {
            page = transactionRepository.findByFundId(fundId, pageable);
        }
        return page.map(FundTransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public List<FeFundTransactionResponse> getFrontendFundTransactions(Long fundId, Long userId) {
        access.requireFund(fundId);
        access.requireMember(fundId, userId);
        return transactionRepository.findByFundIdAndIsApproved(fundId, true).stream()
                .map(mapper::toFeTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TopContributorResponse> getTopContributors(Long fundId, Long userId) {
        access.requireFund(fundId);
        access.requireMember(fundId, userId);
        Map<Long, BigDecimal> contributionMap = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>();
        for (Transaction transaction : transactionRepository.findByFundIdAndIsApproved(fundId, true)) {
            if (TransactionType.INCOME.name().equals(transaction.getType())) {
                Long contributorId = transaction.getUser().getId();
                contributionMap.merge(contributorId, transaction.getAmount(), BigDecimal::add);
                userMap.putIfAbsent(contributorId, transaction.getUser());
            }
        }
        BigDecimal total = contributionMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return contributionMap.entrySet().stream()
                .sorted((left, right) -> right.getValue().compareTo(left.getValue()))
                .map(entry -> {
                    User user = userMap.get(entry.getKey());
                    int percent = total.compareTo(BigDecimal.ZERO) == 0
                            ? 0
                            : entry.getValue().multiply(BigDecimal.valueOf(100))
                                    .divide(total, 0, java.math.RoundingMode.HALF_UP)
                                    .intValue();
                    return TopContributorResponse.builder()
                            .name(user.getDisplayName())
                            .amount(entry.getValue())
                            .percent(percent)
                            .avatar(mapper.defaultAvatar(user))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetChartResponse> getBudgetChart(Long fundId, Long userId) {
        Fund fund = access.requireFund(fundId);
        access.requireMember(fundId, userId);

        List<Transaction> approvedTransactions = transactionRepository.findByFundIdAndIsApproved(fundId, true);
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

        return java.util.stream.IntStream.iterate(5, monthsAgo -> monthsAgo - 1)
                .limit(5)
                .mapToObj(monthsAgo -> currentMonth.minusMonths(monthsAgo))
                .map(monthStart -> BudgetChartResponse.builder()
                        .month("T" + monthStart.getMonthValue())
                        .amount(calculateClosingBalanceAtMonthEnd(
                                fund.getBalance(), approvedTransactions, monthStart).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal calculateClosingBalanceAtMonthEnd(
            BigDecimal currentBalance, List<Transaction> approvedTransactions, LocalDate monthStart) {
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        BigDecimal balance = currentBalance;

        for (Transaction transaction : approvedTransactions) {
            if (transaction.getDate().isAfter(monthEnd)) {
                if (TransactionType.INCOME.name().equals(transaction.getType())) {
                    balance = balance.subtract(transaction.getAmount());
                } else if (TransactionType.EXPENSE.name().equals(transaction.getType())) {
                    balance = balance.add(transaction.getAmount());
                }
            }
        }
        return balance.max(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public FundStatisticsResponse getFundStatistics(Long fundId, Long userId) {
        Fund fund = access.requireFund(fundId);
        access.requireMember(fundId, userId);

        List<FundMember> members = fundMemberRepository.findByFundId(fundId);
        List<Transaction> approvedTransactions = transactionRepository.findByFundIdAndIsApproved(fundId, true);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<Long, BigDecimal> contributionMap = new HashMap<>();
        Map<Long, BigDecimal> spentMap = new HashMap<>();
        Map<Long, String> nameMap = new HashMap<>();

        for (Transaction transaction : approvedTransactions) {
            Long userIdKey = transaction.getUser().getId();
            nameMap.putIfAbsent(userIdKey, transaction.getUser().getDisplayName());
            if (TransactionType.INCOME.name().equals(transaction.getType())) {
                totalIncome = totalIncome.add(transaction.getAmount());
                contributionMap.merge(userIdKey, transaction.getAmount(), BigDecimal::add);
            } else {
                totalExpense = totalExpense.add(transaction.getAmount());
                spentMap.merge(userIdKey, transaction.getAmount(), BigDecimal::add);
            }
        }

        List<FundStatisticsResponse.MemberContribution> contributions = members.stream()
                .map(member -> FundStatisticsResponse.MemberContribution.builder()
                        .userId(member.getUser().getId())
                        .displayName(member.getUser().getDisplayName())
                        .totalContributed(contributionMap.getOrDefault(
                                member.getUser().getId(), BigDecimal.ZERO))
                        .totalSpent(spentMap.getOrDefault(member.getUser().getId(), BigDecimal.ZERO))
                        .build())
                .sorted((left, right) -> right.getTotalContributed().compareTo(left.getTotalContributed()))
                .collect(Collectors.toList());

        List<FundStatisticsResponse.MemberContribution> topMonthly =
                calculateTopMonthlyContributors(fundId, nameMap);
        long totalAll = transactionRepository.countByFundId(fundId);
        long pendingCount = transactionRepository.countByFundIdAndIsApproved(fundId, false);

        return FundStatisticsResponse.builder()
                .fundId(fundId)
                .fundName(fund.getName())
                .currentBalance(fund.getBalance())
                .targetAmount(fund.getTargetAmount())
                .progressPercent(mapper.calculateProgressPercent(fund))
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalMembers(members.size())
                .totalTransactions((int) totalAll)
                .approvedTransactions(approvedTransactions.size())
                .pendingTransactions((int) pendingCount)
                .memberContributions(contributions)
                .topContributorsThisMonth(topMonthly)
                .build();
    }

    private void applyApprovedAmount(Fund fund, Transaction transaction) {
        TransactionType txType = TransactionType.valueOf(transaction.getType());
        if (TransactionType.INCOME == txType) {
            fund.setBalance(fund.getBalance().add(transaction.getAmount()));
        } else if (TransactionType.EXPENSE == txType) {
            fund.setBalance(fund.getBalance().subtract(transaction.getAmount()));
        }
    }

    private void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
    }

    private List<FundStatisticsResponse.MemberContribution> calculateTopMonthlyContributors(
            Long fundId, Map<Long, String> nameMap) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now();
        Map<Long, BigDecimal> monthlyMap = new HashMap<>();
        for (Transaction transaction : transactionRepository.findByFundIdAndIsApprovedAndDateBetween(
                fundId, true, startOfMonth, endOfMonth)) {
            if (TransactionType.INCOME.name().equals(transaction.getType())) {
                monthlyMap.merge(transaction.getUser().getId(), transaction.getAmount(), BigDecimal::add);
                nameMap.putIfAbsent(transaction.getUser().getId(), transaction.getUser().getDisplayName());
            }
        }

        return monthlyMap.entrySet().stream()
                .map(entry -> FundStatisticsResponse.MemberContribution.builder()
                        .userId(entry.getKey())
                        .displayName(nameMap.getOrDefault(entry.getKey(), ""))
                        .totalContributed(entry.getValue())
                        .totalSpent(BigDecimal.ZERO)
                        .build())
                .sorted((left, right) -> right.getTotalContributed().compareTo(left.getTotalContributed()))
                .collect(Collectors.toList());
    }
}
