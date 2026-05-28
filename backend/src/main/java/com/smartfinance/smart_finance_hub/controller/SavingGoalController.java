package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.CreateSavingGoalRequest;
import com.smartfinance.smart_finance_hub.dto.request.GoalTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.PinGoalRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateSavingGoalRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.entity.SavingGoal;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.SavingGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saving-goals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SavingGoalController {

    private final SavingGoalService savingGoalService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavingGoal>>> getAllGoals() {
        Long userId = getCurrentUserId();
        List<SavingGoal> goals = savingGoalService.getAllGoals(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách mục tiêu thành công", goals));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SavingGoal>> createGoal(@Valid @RequestBody CreateSavingGoalRequest request) {
        Long userId = getCurrentUserId();
        SavingGoal goal = savingGoalService.createGoal(userId, request);
        return new ResponseEntity<>(ApiResponse.success("Tạo mục tiêu thành công", goal), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SavingGoal>> updateGoal(
            @PathVariable("id") Long goalId,
            @Valid @RequestBody UpdateSavingGoalRequest request) {
        Long userId = getCurrentUserId();
        SavingGoal goal = savingGoalService.updateGoal(userId, goalId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật mục tiêu thành công", goal));
    }

    @PatchMapping("/{id}/pin")
    public ResponseEntity<ApiResponse<SavingGoal>> pinGoal(
            @PathVariable("id") Long goalId,
            @Valid @RequestBody PinGoalRequest request) {
        Long userId = getCurrentUserId();
        SavingGoal goal = savingGoalService.pinGoal(userId, goalId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái ghim thành công", goal));
    }

    @PatchMapping("/{id}/deposit")
    public ResponseEntity<ApiResponse<SavingGoal>> depositGoal(
            @PathVariable("id") Long goalId,
            @Valid @RequestBody GoalTransactionRequest request) {
        Long userId = getCurrentUserId();
        SavingGoal goal = savingGoalService.depositGoal(userId, goalId, request);
        return ResponseEntity.ok(ApiResponse.success("Nạp tiền vào mục tiêu thành công", goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable("id") Long goalId) {
        Long userId = getCurrentUserId();
        savingGoalService.deleteGoal(userId, goalId);
        return ResponseEntity.ok(ApiResponse.success("Xóa mục tiêu thành công"));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Không thể xác thực người dùng hiện tại!");
    }
}
