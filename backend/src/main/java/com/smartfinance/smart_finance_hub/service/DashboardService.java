package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.response.CashFlowResponse;
import com.smartfinance.smart_finance_hub.dto.response.CategoryExpenseResponse;

import java.util.List;

public interface DashboardService {
    List<CategoryExpenseResponse> getExpenseByCategory(Long userId, int month, int year);
    List<CashFlowResponse> getCashFlowByYear(Long userId, int year);
}
