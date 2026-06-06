package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.response.FinancialSnapshotDTO;
import org.springframework.stereotype.Component;

@Component
public class AiPromptBuilder {

    public String buildChatPrompt(String userMessage, FinancialSnapshotDTO snapshot, String knowledgeContext) {
        return String.format("""
            Bạn là trợ lý tài chính AI của Smart Finance Hub. Hãy trả lời bằng tiếng Việt có dấu, tự nhiên và dễ hiểu.

            NGUYÊN TẮC:
            - Chỉ tư vấn dựa trên dữ liệu tài chính thực tế của người dùng
            - Không bịa số liệu hoặc đưa ra con số không có căn cứ
            - Nếu dùng kiến thức RAG, chỉ trích dẫn từ BỐI CẢNH KIẾN THỨC BỔ TRỢ
            - Luôn đề xuất tham vấn chuyên gia cho quyết định tài chính lớn
            - Giữ câu trả lời ngắn gọn, rõ ý và có hành động cụ thể
            %s

            DỮ LIỆU TÀI CHÍNH THÁNG %s:
            - Thu nhập: %s VND
            - Chi tiêu: %s VND
            - Tiết kiệm ròng: %s VND
            - Chi tiêu theo danh mục: %s
            - Mục tiêu tiết kiệm: %d mục tiêu

            CÂU HỎI CỦA NGƯỜI DÙNG:
            %s
            """,
            knowledgeContext,
            snapshot.getMonth(),
            snapshot.getTotalIncome() != null ? snapshot.getTotalIncome().toPlainString() : "0",
            snapshot.getTotalExpense() != null ? snapshot.getTotalExpense().toPlainString() : "0",
            snapshot.getNetSaving() != null ? snapshot.getNetSaving().toPlainString() : "0",
            snapshot.getExpenseByCategory() != null ? snapshot.getExpenseByCategory().toString() : "{}",
            snapshot.getSavingGoals() != null ? snapshot.getSavingGoals().size() : 0,
            userMessage
        );
    }

    public String buildInsightPrompt(FinancialSnapshotDTO snapshot) {
        return String.format("""
            Bạn là chuyên gia phân tích tài chính cá nhân. Hãy phân tích dữ liệu tài chính sau
            và đưa ra nhận xét ngắn gọn bằng tiếng Việt có dấu.

            THÁNG: %s
            THU NHẬP: %s VND
            CHI TIÊU: %s VND
            TIẾT KIỆM RÒNG: %s VND
            CHI TIÊU THEO DANH MỤC: %s
            THU NHẬP THEO DANH MỤC: %s
            MỤC TIÊU TIẾT KIỆM: %s
            GIAO DỊCH ĐỊNH KỲ SẮP TỚI: %s

            YÊU CẦU:
            1. Tóm tắt tình hình tài chính trong 2-3 câu
            2. Nhận xét khoản chi tiêu lớn nhất
            3. Đánh giá tiến độ tiết kiệm
            4. Đề xuất 2-3 hành động cải thiện cụ thể
            5. Không bịa số liệu, chỉ dùng dữ liệu đã cung cấp
            """,
            snapshot.getMonth(),
            snapshot.getTotalIncome() != null ? snapshot.getTotalIncome().toPlainString() : "0",
            snapshot.getTotalExpense() != null ? snapshot.getTotalExpense().toPlainString() : "0",
            snapshot.getNetSaving() != null ? snapshot.getNetSaving().toPlainString() : "0",
            snapshot.getExpenseByCategory() != null ? snapshot.getExpenseByCategory().toString() : "{}",
            snapshot.getIncomeByCategory() != null ? snapshot.getIncomeByCategory().toString() : "{}",
            snapshot.getSavingGoals() != null ? snapshot.getSavingGoals().toString() : "[]",
            snapshot.getUpcomingRecurring() != null ? snapshot.getUpcomingRecurring().toString() : "[]"
        );
    }

    public String withOutputCorrection(String prompt) {
        return prompt + """

            HỆ THỐNG SỬA LỖI:
            Câu trả lời trước có thể chưa đúng số liệu. Hãy trả lời lại và chỉ sử dụng đúng các số trong DỮ LIỆU TÀI CHÍNH.
            Nếu không chắc, hãy nói chung chung và không tạo thêm số tiền hoặc tỷ lệ mới.
            """;
    }
}
