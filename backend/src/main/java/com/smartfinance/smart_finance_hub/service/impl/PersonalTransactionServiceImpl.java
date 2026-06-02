package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.CreateTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.response.TransactionResponse;
import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.entity.PersonalFund;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.FundStatus;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.repository.PersonalFundRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.PersonalTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalTransactionServiceImpl implements PersonalTransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PersonalFundRepository personalFundRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, Long userId) {
        log.info("createTransaction: userId={}, type={}, amount={}",
                userId, request.getType(), request.getAmount());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi dung!"));

        TransactionType transactionType = parseTransactionType(request.getType());
        Category category = findUsableCategory(request.getCategoryId(), userId);
        validateCategoryType(category, transactionType.name());

        PersonalFund personalFund = null;
        if (request.getPersonalFundId() != null) {
            personalFund = findActiveUserFund(request.getPersonalFundId(), userId);
            applyBalanceImpact(personalFund, transactionType.name(), request.getAmount());
            personalFundRepository.save(personalFund);
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .shareFund(null)
                .personalFund(personalFund)
                .amount(request.getAmount())
                .type(transactionType.name())
                .description(request.getDescription())
                .date(request.getDate())
                .isApproved(true)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("createTransaction success: transactionId={}", saved.getId());
        return TransactionResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            Long userId,
            String type,
            Long categoryId,
            Long personalFundId,
            String search,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        log.info("getTransactions: userId={}, type={}, categoryId={}, personalFundId={}, search={}, range=[{} - {}]",
                userId, type, categoryId, personalFundId, search, startDate, endDate);

        String normalizedType = type != null && !type.isBlank()
                ? parseTransactionType(type).name()
                : null;

        if ((startDate == null) != (endDate == null)) {
            throw new IllegalArgumentException("Phai truyen ca startDate va endDate, hoac khong truyen cai nao!");
        }
        if (startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate phai >= startDate!");
        }

        if (categoryId != null) {
            findUsableCategory(categoryId, userId);
        }
        if (personalFundId != null) {
            findActiveUserFund(personalFundId, userId);
        }

        String normalizedSearch = search != null && !search.isBlank() ? search.trim() : null;

        Page<Transaction> page = transactionRepository.searchPersonalTransactions(
                userId,
                normalizedType,
                categoryId,
                personalFundId,
                normalizedSearch,
                startDate,
                endDate,
                pageable);

        return page.map(TransactionResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long transactionId, Long userId) {
        log.info("getTransactionById: id={}, userId={}", transactionId, userId);
        return TransactionResponse.from(findPersonalTransaction(transactionId, userId));
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(
            Long transactionId, UpdateTransactionRequest request, Long userId) {
        log.info("updateTransaction: id={}, userId={}", transactionId, userId);

        Transaction transaction = findPersonalTransaction(transactionId, userId);
        PersonalFund oldFund = transaction.getPersonalFund();
        String oldType = transaction.getType();
        BigDecimal oldAmount = transaction.getAmount();

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            transaction.setType(parseTransactionType(request.getType()).name());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getDate() != null) {
            transaction.setDate(request.getDate());
        }
        if (request.getCategoryId() != null) {
            transaction.setCategory(findUsableCategory(request.getCategoryId(), userId));
        }
        if (request.getPersonalFundId() != null) {
            transaction.setPersonalFund(findActiveUserFund(request.getPersonalFundId(), userId));
        }

        validateCategoryType(transaction.getCategory(), transaction.getType());

        if (oldFund != null) {
            revertBalanceImpact(oldFund, oldType, oldAmount);
            personalFundRepository.save(oldFund);
        }
        if (transaction.getPersonalFund() != null) {
            applyBalanceImpact(transaction.getPersonalFund(), transaction.getType(), transaction.getAmount());
            personalFundRepository.save(transaction.getPersonalFund());
        }

        Transaction saved = transactionRepository.save(transaction);
        log.info("updateTransaction success: id={}", saved.getId());
        return TransactionResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long transactionId, Long userId) {
        log.info("deleteTransaction: id={}, userId={}", transactionId, userId);
        Transaction transaction = findPersonalTransaction(transactionId, userId);
        if (transaction.getPersonalFund() != null) {
            revertBalanceImpact(transaction.getPersonalFund(), transaction.getType(), transaction.getAmount());
            personalFundRepository.save(transaction.getPersonalFund());
        }
        transactionRepository.delete(transaction);
        log.info("deleteTransaction success: id={}", transactionId);
    }

    private Transaction findPersonalTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Khong tim thay giao dich voi ID: " + transactionId));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Ban khong co quyen truy cap giao dich nay!");
        }
        if (transaction.getShareFund() != null) {
            throw new IllegalArgumentException(
                    "Giao dich nay thuoc quy chung, khong phai giao dich ca nhan!");
        }
        return transaction;
    }

    private Category findUsableCategory(Long categoryId, Long userId) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Khong tim thay danh muc voi ID: " + categoryId));
        validateCategoryOwnership(category, userId);
        return category;
    }

    private PersonalFund findActiveUserFund(Long personalFundId, Long userId) {
        PersonalFund personalFund = personalFundRepository.findByIdAndUserId(personalFundId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Khong tim thay nguon tien voi ID: " + personalFundId));

        if (personalFund.getStatus() != FundStatus.ACTIVE) {
            throw new IllegalArgumentException("Nguon tien '" + personalFund.getName() + "' da bi dong!");
        }
        return personalFund;
    }

    private void applyBalanceImpact(PersonalFund personalFund, String type, BigDecimal amount) {
        if (TransactionType.INCOME.name().equals(type)) {
            personalFund.setBalance(personalFund.getBalance().add(amount));
            return;
        }

        if (personalFund.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "So du nguon tien '" + personalFund.getName() + "' khong du!");
        }
        personalFund.setBalance(personalFund.getBalance().subtract(amount));
    }

    private void revertBalanceImpact(PersonalFund personalFund, String type, BigDecimal amount) {
        if (TransactionType.INCOME.name().equals(type)) {
            personalFund.setBalance(personalFund.getBalance().subtract(amount));
        } else {
            personalFund.setBalance(personalFund.getBalance().add(amount));
        }
    }

    private TransactionType parseTransactionType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Loai giao dich khong hop le! Chi chap nhan: INCOME, EXPENSE");
        }
    }

    private void validateCategoryOwnership(Category category, Long userId) {
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Ban khong co quyen dung danh muc nay!");
        }
    }

    private void validateCategoryType(Category category, String transactionType) {
        if (category != null && !category.getType().equalsIgnoreCase(transactionType)) {
            throw new IllegalArgumentException("Danh muc khong khop voi loai giao dich!");
        }
    }
}
