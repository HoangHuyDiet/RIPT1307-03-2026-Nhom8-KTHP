package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.CreateRecurringSettingRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateRecurringSettingRequest;
import com.smartfinance.smart_finance_hub.dto.response.RecurringSettingResponse;
import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.entity.RecurringSetting;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.RecurringFrequency;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.repository.RecurringSettingRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.RecurringSettingService;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringSettingServiceImpl implements RecurringSettingService {

    private final RecurringSettingRepository recurringSettingRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public RecurringSettingResponse createSetting(CreateRecurringSettingRequest request, Long userId) {
        log.info("createSetting: userId={}, freq={}, type={}",
                userId, request.getFrequency(), request.getType());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        validateCategoryOwnership(category, userId);
        TransactionType transactionType = parseTransactionType(request.getType());
        validateCategoryType(category, transactionType.name());

        RecurringFrequency frequency = parseFrequency(request.getFrequency());
        validateFrequencyFields(frequency, request.getDayOfMonth(), request.getDayOfWeek());
        validateDateRange(request.getStartDate(), request.getEndDate());

        LocalDate nextRunDate = calculateFirstRunDate(
                frequency, request.getStartDate(), request.getDayOfMonth(), request.getDayOfWeek());

        RecurringSetting setting = RecurringSetting.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .type(transactionType.name())
                .description(request.getDescription())
                .frequency(frequency)
                .dayOfMonth(request.getDayOfMonth())
                .dayOfWeek(request.getDayOfWeek())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextRunDate(nextRunDate)
                .isActive(true)
                .build();

        RecurringSetting saved = recurringSettingRepository.save(setting);
        log.info("createSetting success: settingId={}", saved.getId());
        return RecurringSettingResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringSettingResponse> getSettingsByUser(Long userId) {
        log.info("getSettingsByUser: userId={}", userId);
        return recurringSettingRepository.findByUserId(userId)
                .stream()
                .map(RecurringSettingResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringSettingResponse> getSettingsByUserAndActive(Long userId, Boolean active) {
        log.info("getSettingsByUserAndActive: userId={}, active={}", userId, active);
        return recurringSettingRepository.findByUserIdAndIsActive(userId, active)
                .stream()
                .map(RecurringSettingResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecurringSettingResponse updateSetting(
            Long settingId, UpdateRecurringSettingRequest request, Long userId) {
        log.info("updateSetting: settingId={}, userId={}", settingId, userId);

        RecurringSetting setting = findOwnedSetting(settingId, userId);
        boolean needRecalculate = false;

        if (request.getAmount() != null) {
            setting.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            setting.setType(parseTransactionType(request.getType()).name());
        }
        if (request.getDescription() != null) {
            setting.setDescription(request.getDescription());
        }
        if (request.getFrequency() != null) {
            setting.setFrequency(parseFrequency(request.getFrequency()));
            needRecalculate = true;
        }
        if (request.getDayOfMonth() != null) {
            setting.setDayOfMonth(request.getDayOfMonth());
            needRecalculate = true;
        }
        if (request.getDayOfWeek() != null) {
            setting.setDayOfWeek(request.getDayOfWeek());
            needRecalculate = true;
        }
        if (request.getStartDate() != null) {
            setting.setStartDate(request.getStartDate());
            needRecalculate = true;
        }
        if (request.getEndDate() != null) {
            setting.setEndDate(request.getEndDate());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại!"));
            validateCategoryOwnership(category, userId);
            setting.setCategory(category);
        }

        validateFrequencyFields(setting.getFrequency(), setting.getDayOfMonth(), setting.getDayOfWeek());
        validateDateRange(setting.getStartDate(), setting.getEndDate());
        validateCategoryType(setting.getCategory(), setting.getType());

        if (needRecalculate) {
            LocalDate newNextRun = calculateFirstRunDate(
                    setting.getFrequency(), setting.getStartDate(),
                    setting.getDayOfMonth(), setting.getDayOfWeek());
            setting.setNextRunDate(newNextRun);
            log.info("Recalculated nextRunDate={} for settingId={}", newNextRun, settingId);
        }

        RecurringSetting saved = recurringSettingRepository.save(setting);
        log.info("updateSetting success: settingId={}", saved.getId());
        return RecurringSettingResponse.from(saved);
    }

    @Override
    @Transactional
    public void deactivateSetting(Long settingId, Long userId) {
        log.info("deactivateSetting: settingId={}, userId={}", settingId, userId);
        RecurringSetting setting = findOwnedSetting(settingId, userId);
        setting.setIsActive(false);
        recurringSettingRepository.save(setting);
        log.info("deactivateSetting success: settingId={}", settingId);
    }

    private RecurringSetting findOwnedSetting(Long settingId, Long userId) {
        RecurringSetting setting = recurringSettingRepository.findById(settingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy cấu hình với ID: " + settingId));
        if (!setting.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập cấu hình này!");
        }
        return setting;
    }

    private TransactionType parseTransactionType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Loại không hợp lệ! Chỉ: INCOME, EXPENSE");
        }
    }

    private RecurringFrequency parseFrequency(String frequency) {
        try {
            return RecurringFrequency.valueOf(frequency.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tần suất không hợp lệ! Chỉ: DAILY, WEEKLY, MONTHLY");
        }
    }

    private void validateCategoryOwnership(Category category, Long userId) {
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền dùng danh mục này!");
        }
    }

    private void validateCategoryType(Category category, String transactionType) {
        if (!category.getType().equalsIgnoreCase(transactionType)) {
            throw new IllegalArgumentException("Danh mục không khớp với loại giao dịch!");
        }
    }

    private void validateFrequencyFields(
            RecurringFrequency frequency, Integer dayOfMonth, Integer dayOfWeek) {
        if (frequency == RecurringFrequency.MONTHLY) {
            if (dayOfMonth == null) {
                throw new IllegalArgumentException("MONTHLY yêu cầu dayOfMonth!");
            }
            if (dayOfMonth < 1 || dayOfMonth > 31) {
                throw new IllegalArgumentException("dayOfMonth phải từ 1 đến 31!");
            }
        }
        if (frequency == RecurringFrequency.WEEKLY) {
            if (dayOfWeek == null) {
                throw new IllegalArgumentException("WEEKLY yêu cầu dayOfWeek!");
            }
            if (dayOfWeek < 1 || dayOfWeek > 7) {
                throw new IllegalArgumentException("dayOfWeek phải từ 1 (MON) đến 7 (SUN)!");
            }
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate phải >= startDate!");
        }
    }

    private LocalDate calculateFirstRunDate(
            RecurringFrequency frequency, LocalDate startDate, Integer dayOfMonth, Integer dayOfWeek) {
        switch (frequency) {
            case DAILY:
                return startDate;
            case WEEKLY:
                LocalDate weekDate = startDate;
                while (weekDate.getDayOfWeek().getValue() != dayOfWeek) {
                    weekDate = weekDate.plusDays(1);
                }
                return weekDate;
            case MONTHLY:
                int day = Math.min(dayOfMonth, startDate.lengthOfMonth());
                LocalDate monthDate = startDate.withDayOfMonth(day);
                if (monthDate.isBefore(startDate)) {
                    monthDate = monthDate.plusMonths(1);
                    monthDate = monthDate.withDayOfMonth(
                            Math.min(dayOfMonth, monthDate.lengthOfMonth()));
                }
                return monthDate;
            default:
                throw new IllegalArgumentException("Tần suất không hợp lệ!");
        }
    }
}
