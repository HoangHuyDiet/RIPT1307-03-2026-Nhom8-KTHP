package com.smartfinance.smart_finance_hub.enums;

/**
 * Phạm vi đồng ý chia sẻ dữ liệu tài chính.
 * Dashboard cá nhân mặc định dùng toàn bộ scope.
 * Consultation bắt buộc người dùng chọn scope trước khi gửi cho chuyên viên.
 */
public enum ConsentScope {
    TRANSACTIONS,    // totalIncome, totalExpense, expenseByCategory, upcomingRecurring
    SAVING_GOALS     // savingGoalProgress
}
