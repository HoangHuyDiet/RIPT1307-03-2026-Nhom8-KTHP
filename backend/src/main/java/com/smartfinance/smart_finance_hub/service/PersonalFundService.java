package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.CreatePersonalFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.InternalTransferRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdatePersonalFundRequest;
import com.smartfinance.smart_finance_hub.dto.response.AssetAllocationResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundBalanceHistoryResponse;
import com.smartfinance.smart_finance_hub.dto.response.PersonalFundResponse;
import com.smartfinance.smart_finance_hub.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.smartfinance.smart_finance_hub.dto.response.PersonalFundSummaryResponse;
import com.smartfinance.smart_finance_hub.dto.request.PayBillRequest;

public interface PersonalFundService {

    PersonalFundResponse createFund(Long userId, CreatePersonalFundRequest request);

    List<PersonalFundResponse> getAllFunds(Long userId);

    PersonalFundResponse getFundById(Long userId, Long fundId);

    PersonalFundResponse updateFund(Long userId, Long fundId, UpdatePersonalFundRequest request);

    void closeFund(Long userId, Long fundId);

    BigDecimal getTotalAssets(Long userId);

    void internalTransfer(Long userId, InternalTransferRequest request);

    Page<TransactionResponse> getFundTransactions(Long userId, Long fundId, Pageable pageable);

    List<AssetAllocationResponse> getAssetAllocation(Long userId);

    List<FundBalanceHistoryResponse> getFundBalanceHistory(Long userId, Long fundId,
                                                           LocalDate startDate, LocalDate endDate);

    PersonalFundSummaryResponse getSummary(Long userId);

    void payBill(Long userId, PayBillRequest request);

    List<FundBalanceHistoryResponse> getGlobalBalanceHistory(Long userId, LocalDate startDate, LocalDate endDate);

    List<TransactionResponse> getRecentTransactions(Long userId);

    void deposit(Long userId, Long fundId, BigDecimal amount, String description, Long categoryId, LocalDate date);
}
