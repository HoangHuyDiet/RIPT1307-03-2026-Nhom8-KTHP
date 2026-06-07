package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.response.FinancialSnapshotDTO;
import com.smartfinance.smart_finance_hub.enums.ConsentScope;

import java.util.Set;

public interface FinancialSnapshotService {

    /**
     * Build bản chụp tài chính cho user, chỉ bao gồm dữ liệu theo consent scope.
     * @param userId ID người dùng
     * @param month tháng dạng "YYYY-MM"
     * @param scopes phạm vi dữ liệu được phép
     */
    FinancialSnapshotDTO buildSnapshot(Long userId, String month, Set<ConsentScope> scopes);

    /**
     * Build snapshot đầy đủ (tất cả scopes) — dùng cho dashboard cá nhân.
     */
    FinancialSnapshotDTO buildFullSnapshot(Long userId, String month);
}
