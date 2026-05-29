package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.CreateTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.response.TransactionResponse;
import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.PersonalTransactionService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalTransactionServiceImpl implements PersonalTransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, Long userId) {
        log.info("createTransaction: userId={}, type={}, amount={}",
                userId, request.getType(), request.getAmount());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        validateCategoryOwnership(category, userId);
        TransactionType transactionType = parseTransactionType(request.getType());
        validateCategoryType(category, transactionType.name());

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .shareFund(null)
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
            Long userId, String type, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("getTransactions: userId={}, type={}, range=[{} - {}]",
                userId, type, startDate, endDate);

        boolean hasType = type != null && !type.isBlank();
        String normalizedType = hasType ? parseTransactionType(type).name() : null;

        if ((startDate == null) != (endDate == null)) {
            throw new IllegalArgumentException(
                    "Phải truyền cả startDate và endDate, hoặc không truyền cái nào!");
        }

        boolean hasDateRange = startDate != null && endDate != null;
        if (hasDateRange && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate phải >= startDate!");
        }

        Page<Transaction> page;
        if (normalizedType != null && hasDateRange) {
            page = transactionRepository.findByUserIdAndTypeAndDateBetweenAndShareFundIsNull(
                    userId, normalizedType, startDate, endDate, pageable);
        } else if (normalizedType != null) {
            page = transactionRepository.findByUserIdAndTypeAndShareFundIsNull(
                    userId, normalizedType, pageable);
        } else if (hasDateRange) {
            page = transactionRepository.findByUserIdAndDateBetweenAndShareFundIsNull(
                    userId, startDate, endDate, pageable);
        } else {
            page = transactionRepository.findByUserIdAndShareFundIsNull(userId, pageable);
        }

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
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy danh mục với ID: " + request.getCategoryId()));
            validateCategoryOwnership(category, userId);
            transaction.setCategory(category);
        }

        validateCategoryType(transaction.getCategory(), transaction.getType());

        Transaction saved = transactionRepository.save(transaction);
        log.info("updateTransaction success: id={}", saved.getId());
        return TransactionResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long transactionId, Long userId) {
        log.info("deleteTransaction: id={}, userId={}", transactionId, userId);
        Transaction transaction = findPersonalTransaction(transactionId, userId);
        transactionRepository.delete(transaction);
        log.info("deleteTransaction success: id={}", transactionId);
    }

    private Transaction findPersonalTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy giao dịch với ID: " + transactionId));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập giao dịch này!");
        }
        if (transaction.getShareFund() != null) {
            throw new IllegalArgumentException(
                    "Giao dịch này thuộc quỹ chung, không phải giao dịch cá nhân!");
        }
        return transaction;
    }

    private TransactionType parseTransactionType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Loại giao dịch không hợp lệ! Chỉ chấp nhận: INCOME, EXPENSE");
        }
    }

    private void validateCategoryOwnership(Category category, Long userId) {
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền dùng danh mục này!");
        }
    }

    private void validateCategoryType(Category category, String transactionType) {
        if (category != null && !category.getType().equalsIgnoreCase(transactionType)) {
            throw new IllegalArgumentException("Danh mục không khớp với loại giao dịch!");
        }
    }
}
