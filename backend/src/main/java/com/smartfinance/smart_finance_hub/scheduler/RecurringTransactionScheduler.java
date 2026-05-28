package com.smartfinance.smart_finance_hub.scheduler;

import com.smartfinance.smart_finance_hub.repository.RecurringSettingRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionScheduler {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final RecurringSettingRepository recurringSettingRepository;
    private final RecurringTransactionProcessor processor;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now(VN_ZONE);
        log.info("=== RECURRING SCHEDULER START: {} ===", today);

        List<Long> dueSettingIds = recurringSettingRepository.findDueSettingIds(today);
        log.info("Found {} due recurring settings", dueSettingIds.size());

        int success = 0;
        int failed = 0;
        for (Long settingId : dueSettingIds) {
            try {
                processor.process(settingId, today);
                success++;
            } catch (Exception e) {
                failed++;
                log.error("Failed to process settingId={}: {}", settingId, e.getMessage(), e);
            }
        }

        log.info("=== RECURRING SCHEDULER END: success={}, failed={} ===", success, failed);
    }
}
