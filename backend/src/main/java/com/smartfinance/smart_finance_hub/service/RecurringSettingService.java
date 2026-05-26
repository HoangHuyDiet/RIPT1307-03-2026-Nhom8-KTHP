package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.CreateRecurringSettingRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateRecurringSettingRequest;
import com.smartfinance.smart_finance_hub.dto.response.RecurringSettingResponse;
import java.util.List;

public interface RecurringSettingService {

    RecurringSettingResponse createSetting(CreateRecurringSettingRequest request, Long userId);

    List<RecurringSettingResponse> getSettingsByUser(Long userId);

    List<RecurringSettingResponse> getSettingsByUserAndActive(Long userId, Boolean active);

    RecurringSettingResponse updateSetting(Long settingId, UpdateRecurringSettingRequest request, Long userId);

    void deactivateSetting(Long settingId, Long userId);
}
