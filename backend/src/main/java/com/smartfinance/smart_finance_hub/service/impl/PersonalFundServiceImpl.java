package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.CreatePersonalFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.InternalTransferRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdatePersonalFundRequest;
import com.smartfinance.smart_finance_hub.dto.response.AssetAllocationResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundBalanceHistoryResponse;
import com.smartfinance.smart_finance_hub.dto.response.PersonalFundResponse;
import com.smartfinance.smart_finance_hub.dto.response.TransactionResponse;
import com.smartfinance.smart_finance_hub.entity.PersonalFund;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.FundStatus;
import com.smartfinance.smart_finance_hub.enums.PersonalFundType;
import com.smartfinance.smart_finance_hub.repository.PersonalFundRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.PersonalFundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.dto.response.PersonalFundSummaryResponse;
import com.smartfinance.smart_finance_hub.dto.request.PayBillRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalFundServiceImpl implements PersonalFundService {

    private final PersonalFundRepository personalFundRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public PersonalFundResponse createFund(Long userId, CreatePersonalFundRequest request) {
        log.info("createFund: userId={}, name={}, type={}", userId, request.getName(), request.getFundType());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        PersonalFundType fundType = parseFundType(request.getFundType());

        PersonalFund fund = PersonalFund.builder()
                .user(user)
                .name(request.getName())
                .fundType(fundType)
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .description(request.getDescription())
                .status(FundStatus.ACTIVE)
                .build();

        PersonalFund saved = personalFundRepository.save(fund);
        log.info("createFund success: fundId={}", saved.getId());
        return PersonalFundResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalFundResponse> getAllFunds(Long userId) {
        log.info("getAllFunds: userId={}", userId);
        List<PersonalFund> funds = personalFundRepository.findByUserIdAndStatus(userId, FundStatus.ACTIVE);
        return funds.stream()
                .map(PersonalFundResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalFundResponse getFundById(Long userId, Long fundId) {
        log.info("getFundById: userId={}, fundId={}", userId, fundId);
        PersonalFund fund = findUserFund(userId, fundId);
        return PersonalFundResponse.from(fund);
    }

    @Override
    @Transactional
    public PersonalFundResponse updateFund(Long userId, Long fundId, UpdatePersonalFundRequest request) {
        log.info("updateFund: userId={}, fundId={}", userId, fundId);

        PersonalFund fund = findUserFund(userId, fundId);
        validateFundActive(fund);

        if (request.getName() != null && !request.getName().isBlank()) {
            fund.setName(request.getName());
        }
        if (request.getDescription() != null) {
            fund.setDescription(request.getDescription());
        }

        PersonalFund saved = personalFundRepository.save(fund);
        log.info("updateFund success: fundId={}", saved.getId());
        return PersonalFundResponse.from(saved);
    }

    @Override
    @Transactional
    public void closeFund(Long userId, Long fundId) {
        log.info("closeFund: userId={}, fundId={}", userId, fundId);

        PersonalFund fund = findUserFund(userId, fundId);
        validateFundActive(fund);

        fund.setStatus(FundStatus.CLOSED);
        personalFundRepository.save(fund);
        log.info("closeFund success: fundId={}", fundId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalAssets(Long userId) {
        log.info("getTotalAssets: userId={}", userId);
        List<PersonalFund> funds = personalFundRepository.findByUserIdAndStatus(userId, FundStatus.ACTIVE);
        return funds.stream()
                .map(PersonalFund::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public void internalTransfer(Long userId, InternalTransferRequest request) {
        log.info("internalTransfer: userId={}, from={}, to={}, amount={}",
                userId, request.getSourceId(), request.getTargetId(), request.getAmount());

        if (request.getSourceId().equals(request.getTargetId())) {
            throw new IllegalArgumentException("Quỹ nguồn và quỹ đích không được trùng nhau!");
        }

        PersonalFund sourceFund = findUserFund(userId, request.getSourceId());
        PersonalFund destFund = findUserFund(userId, request.getTargetId());

        validateFundActive(sourceFund);
        validateFundActive(destFund);

        if (sourceFund.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Số dư quỹ nguồn không đủ! Số dư hiện tại: " + sourceFund.getBalance());
        }

        sourceFund.setBalance(sourceFund.getBalance().subtract(request.getAmount()));
        destFund.setBalance(destFund.getBalance().add(request.getAmount()));

        personalFundRepository.save(sourceFund);
        personalFundRepository.save(destFund);

        String desc = request.getDescription() != null ? request.getDescription()
                : "Chuyển tiền: " + sourceFund.getName() + " → " + destFund.getName();

        User user = sourceFund.getUser();

        Transaction expenseTx = Transaction.builder()
                .user(user)
                .personalFund(sourceFund)
                .amount(request.getAmount())
                .type("EXPENSE")
                .description("[Chuyển đi] " + desc)
                .date(LocalDate.now())
                .isApproved(true)
                .build();

        Transaction incomeTx = Transaction.builder()
                .user(user)
                .personalFund(destFund)
                .amount(request.getAmount())
                .type("INCOME")
                .description("[Nhận chuyển] " + desc)
                .date(LocalDate.now())
                .isApproved(true)
                .build();

        Transaction savedExpense = transactionRepository.save(expenseTx);
        Transaction savedIncome = transactionRepository.save(incomeTx);

        savedExpense.setLinkedTransactionId(savedIncome.getId());
        savedIncome.setLinkedTransactionId(savedExpense.getId());

        transactionRepository.save(savedExpense);
        transactionRepository.save(savedIncome);

        log.info("internalTransfer success: expenseTxId={}, incomeTxId={}",
                savedExpense.getId(), savedIncome.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getFundTransactions(Long userId, Long fundId, Pageable pageable) {
        log.info("getFundTransactions: userId={}, fundId={}", userId, fundId);
        findUserFund(userId, fundId);
        Page<Transaction> page = transactionRepository.findByPersonalFundId(fundId, pageable);
        return page.map(TransactionResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetAllocationResponse> getAssetAllocation(Long userId) {
        log.info("getAssetAllocation: userId={}", userId);

        List<PersonalFund> funds = personalFundRepository.findByUserIdAndStatus(userId, FundStatus.ACTIVE);
        BigDecimal totalAssets = funds.stream()
                .map(PersonalFund::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return funds.stream()
                .map(fund -> AssetAllocationResponse.builder()
                        .fundId(fund.getId())
                        .fundName(fund.getName())
                        .fundType(fund.getFundType().name())
                        .balance(fund.getBalance())
                        .percentage(totalAssets.compareTo(BigDecimal.ZERO) > 0
                                ? fund.getBalance()
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(totalAssets, 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundBalanceHistoryResponse> getFundBalanceHistory(Long userId, Long fundId,
                                                                  LocalDate startDate, LocalDate endDate) {
        log.info("getFundBalanceHistory: userId={}, fundId={}, range=[{} - {}]",
                userId, fundId, startDate, endDate);

        PersonalFund fund = findUserFund(userId, fundId);

        List<Transaction> transactions = transactionRepository
                .findByPersonalFundIdAndDateBetweenOrderByDateAsc(fundId, startDate, endDate);

        BigDecimal currentBalance = fund.getBalance();
        List<Transaction> allTxAfterStart = transactionRepository
                .findByPersonalFundIdAndDateBetweenOrderByDateAsc(fundId, startDate, LocalDate.now());

        BigDecimal balanceAtStart = currentBalance;
        for (Transaction tx : allTxAfterStart) {
            if ("INCOME".equals(tx.getType())) {
                balanceAtStart = balanceAtStart.subtract(tx.getAmount());
            } else {
                balanceAtStart = balanceAtStart.add(tx.getAmount());
            }
        }

        List<FundBalanceHistoryResponse> history = new ArrayList<>();
        BigDecimal runningBalance = balanceAtStart;
        LocalDate cursor = startDate;

        int txIndex = 0;
        while (!cursor.isAfter(endDate)) {
            while (txIndex < transactions.size() && transactions.get(txIndex).getDate().equals(cursor)) {
                Transaction tx = transactions.get(txIndex);
                if ("INCOME".equals(tx.getType())) {
                    runningBalance = runningBalance.add(tx.getAmount());
                } else {
                    runningBalance = runningBalance.subtract(tx.getAmount());
                }
                txIndex++;
            }

            history.add(FundBalanceHistoryResponse.builder()
                    .date(cursor)
                    .balance(runningBalance)
                    .build());

            cursor = cursor.plusDays(1);
        }

        return history;
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalFundSummaryResponse getSummary(Long userId) {
        log.info("getSummary: userId={}", userId);
        BigDecimal totalAssets = getTotalAssets(userId);
        return PersonalFundSummaryResponse.builder()
                .totalAssets(totalAssets)
                .growthRate(12.5)
                .build();
    }

    @Override
    @Transactional
    public void payBill(Long userId, PayBillRequest request) {
        log.info("payBill: userId={}, amount={}, fundId={}", userId, request.getAmount(), request.getFundId());
        PersonalFund fund = findUserFund(userId, request.getFundId());
        validateFundActive(fund);

        if (fund.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Số dư quỹ không đủ để thanh toán!");
        }

        fund.setBalance(fund.getBalance().subtract(request.getAmount()));
        personalFundRepository.save(fund);

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }
        if (category == null) {
            category = categoryRepository.findAvailableCategories(userId, "EXPENSE")
                    .stream().findFirst().orElse(null);
        }

        Transaction tx = Transaction.builder()
                .user(fund.getUser())
                .personalFund(fund)
                .category(category)
                .amount(request.getAmount())
                .type("EXPENSE")
                .description(request.getContent() != null ? request.getContent() : "Thanh toán hóa đơn")
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .isApproved(true)
                .build();

        transactionRepository.save(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundBalanceHistoryResponse> getGlobalBalanceHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("getGlobalBalanceHistory: userId={}, range=[{} - {}]", userId, startDate, endDate);
        List<PersonalFund> funds = personalFundRepository.findByUserIdAndStatus(userId, FundStatus.ACTIVE);

        List<FundBalanceHistoryResponse> globalHistory = new ArrayList<>();
        if (funds.isEmpty()) {
            LocalDate cursor = startDate;
            while (!cursor.isAfter(endDate)) {
                globalHistory.add(FundBalanceHistoryResponse.builder()
                        .date(cursor)
                        .balance(BigDecimal.ZERO)
                        .build());
                cursor = cursor.plusDays(1);
            }
            return globalHistory;
        }

        java.util.Map<LocalDate, BigDecimal> dateToBalance = new java.util.LinkedHashMap<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            dateToBalance.put(cursor, BigDecimal.ZERO);
            cursor = cursor.plusDays(1);
        }

        for (PersonalFund fund : funds) {
            List<FundBalanceHistoryResponse> fundHistory = getFundBalanceHistory(userId, fund.getId(), startDate, endDate);
            for (FundBalanceHistoryResponse pt : fundHistory) {
                if (dateToBalance.containsKey(pt.getDate())) {
                    dateToBalance.put(pt.getDate(), dateToBalance.get(pt.getDate()).add(pt.getBalance()));
                }
            }
        }

        for (java.util.Map.Entry<LocalDate, BigDecimal> entry : dateToBalance.entrySet()) {
            globalHistory.add(FundBalanceHistoryResponse.builder()
                    .date(entry.getKey())
                    .balance(entry.getValue())
                    .build());
        }

        return globalHistory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions(Long userId) {
        log.info("getRecentTransactions: userId={}", userId);
        org.springframework.data.domain.Pageable limit20 = org.springframework.data.domain.PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("date").descending().and(org.springframework.data.domain.Sort.by("createdAt").descending()));
        org.springframework.data.domain.Page<Transaction> page = transactionRepository.findByUserIdAndShareFundIsNull(userId, limit20);
        return page.getContent().stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deposit(Long userId, Long fundId, BigDecimal amount, String description) {
        log.info("deposit: userId={}, fundId={}, amount={}", userId, fundId, amount);
        PersonalFund fund = findUserFund(userId, fundId);
        validateFundActive(fund);

        fund.setBalance(fund.getBalance().add(amount));
        personalFundRepository.save(fund);

        Category category = categoryRepository.findAvailableCategories(userId, "INCOME")
                .stream().findFirst().orElse(null);

        Transaction tx = Transaction.builder()
                .user(fund.getUser())
                .personalFund(fund)
                .category(category)
                .amount(amount)
                .type("INCOME")
                .description(description != null && !description.isBlank() ? description : "Nạp tiền vào quỹ (Chuyển khoản ngoài)")
                .date(LocalDate.now())
                .isApproved(true)
                .build();

        transactionRepository.save(tx);
    }

    private PersonalFund findUserFund(Long userId, Long fundId) {
        return personalFundRepository.findByIdAndUserId(fundId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy quỹ cá nhân với ID: " + fundId));
    }

    private void validateFundActive(PersonalFund fund) {
        if (fund.getStatus() != FundStatus.ACTIVE) {
            throw new IllegalArgumentException("Quỹ '" + fund.getName() + "' đã bị đóng, không thể thao tác!");
        }
    }

    private PersonalFundType parseFundType(String type) {
        try {
            return PersonalFundType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Loại quỹ không hợp lệ! Chỉ chấp nhận: CASH, BANK_ACCOUNT, CREDIT_CARD, E_WALLET, INVESTMENT");
        }
    }
}
