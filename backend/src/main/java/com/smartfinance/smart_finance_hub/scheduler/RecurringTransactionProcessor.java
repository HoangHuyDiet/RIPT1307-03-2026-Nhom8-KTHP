package com.smartfinance.smart_finance_hub.scheduler;

import com.smartfinance.smart_finance_hub.entity.RecurringSetting;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.repository.RecurringSettingRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionProcessor {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final RecurringSettingRepository recurringSettingRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long settingId, LocalDate today) {
        RecurringSetting setting = recurringSettingRepository.findByIdWithRelations(settingId)
                .orElseThrow(() -> new IllegalArgumentException("Setting không tồn tại: " + settingId));

        log.info("Processing settingId={}, userId={}, type={}, amount={}",
                setting.getId(), setting.getUser().getId(), setting.getType(), setting.getAmount());

        if (setting.getLastRunAt() != null && setting.getLastRunAt().toLocalDate().isEqual(today)) {
            log.warn("settingId={} đã chạy ngày {}, skip!", setting.getId(), today);
            return;
        }

        Transaction transaction = Transaction.builder()
                .user(setting.getUser())
                .category(setting.getCategory())
                .shareFund(null)
                .amount(setting.getAmount())
                .type(setting.getType())
                .description("[Tự động] " + (setting.getDescription() != null
                        ? setting.getDescription() : "Giao dịch định kỳ"))
                .date(today)
                .isApproved(true)
                .build();

        transactionRepository.save(transaction);
        log.info("Created recurring transaction: transactionId={}", transaction.getId());

        LocalDate nextRun = calculateNextRunDate(setting);
        if (setting.getEndDate() != null && nextRun.isAfter(setting.getEndDate())) {
            setting.setIsActive(false);
            log.info("settingId={} expired and was deactivated", setting.getId());
        } else {
            setting.setNextRunDate(nextRun);
        }

        setting.setLastRunAt(LocalDateTime.now(VN_ZONE));
        recurringSettingRepository.save(setting);
    }

    private LocalDate calculateNextRunDate(RecurringSetting setting) {
        LocalDate current = setting.getNextRunDate();

        switch (setting.getFrequency()) {
            case DAILY:
                return current.plusDays(1);
            case WEEKLY:
                return current.plusWeeks(1);
            case MONTHLY:
                LocalDate nextMonth = current.plusMonths(1);
                int targetDay = setting.getDayOfMonth() != null
                        ? setting.getDayOfMonth() : current.getDayOfMonth();
                int safeDay = Math.min(targetDay, nextMonth.lengthOfMonth());
                return nextMonth.withDayOfMonth(safeDay);
            default:
                return current.plusMonths(1);
        }
    }
}
