package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.response.CashFlowResponse;
import com.smartfinance.smart_finance_hub.dto.response.CategoryExpenseResponse;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import com.smartfinance.smart_finance_hub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryExpenseResponse> getExpenseByCategory(Long userId, int month, int year) {
        return transactionRepository.getExpenseByCategory(userId, month, year);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashFlowResponse> getCashFlowByYear(Long userId, int year) {
        return transactionRepository.getCashFlowByYear(userId, year);
    }
}
