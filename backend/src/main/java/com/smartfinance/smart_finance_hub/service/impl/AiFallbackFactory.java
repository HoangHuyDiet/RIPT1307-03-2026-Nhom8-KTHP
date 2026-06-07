package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.response.FinancialSnapshotDTO;
import org.springframework.stereotype.Component;

@Component
public class AiFallbackFactory {

    public String aiDisabled() {
        return "Tính năng AI hiện chưa được kích hoạt. Bạn vẫn có thể xem các gợi ý tài chính miễn phí trong Dashboard.";
    }

    public String aiUnavailable() {
        return "Xin lỗi, hệ thống AI tạm thời chưa thể xử lý yêu cầu. Vui lòng thử lại sau.";
    }

    public String rateLimited() {
        return "Bạn đang gửi yêu cầu AI quá nhanh. Vui lòng đợi một lát rồi thử lại.";
    }

    public String outputValidationFailed(FinancialSnapshotDTO snapshot) {
        return String.format("""
            Hệ thống chưa thể tạo câu trả lời AI đủ an toàn tại thời điểm này.
            Dữ liệu tài chính thực tế của bạn trong tháng %s:
            - Thu nhập: %s VND
            - Chi tiêu: %s VND
            - Tiết kiệm ròng: %s VND

            Vui lòng kiểm tra lại báo cáo tài chính hoặc gửi yêu cầu tư vấn cho chuyên viên hỗ trợ.
            """,
            snapshot.getMonth(),
            snapshot.getTotalIncome() != null ? snapshot.getTotalIncome().toPlainString() : "0",
            snapshot.getTotalExpense() != null ? snapshot.getTotalExpense().toPlainString() : "0",
            snapshot.getNetSaving() != null ? snapshot.getNetSaving().toPlainString() : "0"
        );
    }
}
