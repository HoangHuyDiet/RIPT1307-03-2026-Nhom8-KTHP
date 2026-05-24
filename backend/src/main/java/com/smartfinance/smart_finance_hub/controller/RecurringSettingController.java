package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.CreateRecurringSettingRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateRecurringSettingRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.dto.response.RecurringSettingResponse;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.RecurringSettingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recurring-settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class RecurringSettingController {

    private final RecurringSettingService recurringSettingService;

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringSettingResponse>> create(
            @Valid @RequestBody CreateRecurringSettingRequest request) {
        Long userId = getCurrentUserId();
        RecurringSettingResponse data = recurringSettingService.createSetting(request, userId);
        return new ResponseEntity<>(
                ApiResponse.success("Tạo cấu hình định kỳ thành công!", data), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringSettingResponse>>> list(
            @RequestParam(required = false) Boolean active) {
        Long userId = getCurrentUserId();
        List<RecurringSettingResponse> data;
        if (active != null) {
            data = recurringSettingService.getSettingsByUserAndActive(userId, active);
        } else {
            data = recurringSettingService.getSettingsByUser(userId);
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cấu hình thành công!", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringSettingResponse>> update(
            @PathVariable("id") Long settingId,
            @Valid @RequestBody UpdateRecurringSettingRequest request) {
        Long userId = getCurrentUserId();
        RecurringSettingResponse data = recurringSettingService.updateSetting(settingId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cấu hình thành công!", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable("id") Long settingId) {
        Long userId = getCurrentUserId();
        recurringSettingService.deactivateSetting(settingId, userId);
        return ResponseEntity.ok(ApiResponse.success("Đã hủy cấu hình định kỳ!"));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Không thể xác thực người dùng hiện tại!");
    }
}
