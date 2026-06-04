package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.CreateTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.response.TransactionResponse;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersonalTransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request, Long userId);

    Page<TransactionResponse> getTransactions(
            Long userId,
            String type,
            Long categoryId,
            Long personalFundId,
            String search,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);

    TransactionResponse getTransactionById(Long transactionId, Long userId);

    TransactionResponse updateTransaction(Long transactionId, UpdateTransactionRequest request, Long userId);

    void deleteTransaction(Long transactionId, Long userId);
}
