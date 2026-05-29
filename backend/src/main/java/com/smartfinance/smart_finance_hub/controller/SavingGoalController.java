package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.SavingGoalDepositRequest;
import com.smartfinance.smart_finance_hub.dto.request.SavingGoalRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.dto.response.SavingGoalResponse;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.SavingGoalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saving-goals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class SavingGoalController {

    private final SavingGoalService savingGoalService;

    @PostMapping
    public ResponseEntity<ApiResponse<SavingGoalResponse>> createGoal(
            @Valid @RequestBody SavingGoalRequest request) {
        Long userId = getCurrentUserId();
        SavingGoalResponse data = savingGoalService.createGoal(request, userId);
        return new ResponseEntity<>(ApiResponse.success("Saving goal created successfully", data), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavingGoalResponse>>> getMyGoals() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", savingGoalService.getMyGoals(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SavingGoalResponse>> getGoalById(@PathVariable("id") Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("OK", savingGoalService.getGoalById(id, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SavingGoalResponse>> updateGoal(
            @PathVariable("id") Long id,
            @Valid @RequestBody SavingGoalRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Saving goal updated successfully", 
                savingGoalService.updateGoal(id, request, userId)));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<ApiResponse<SavingGoalResponse>> deposit(
            @PathVariable("id") Long id,
            @Valid @RequestBody SavingGoalDepositRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Deposit processed successfully", 
                savingGoalService.deposit(id, request, userId)));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<SavingGoalResponse>> withdraw(
            @PathVariable("id") Long id,
            @Valid @RequestBody SavingGoalDepositRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Withdrawal processed successfully", 
                savingGoalService.withdraw(id, request, userId)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SavingGoalResponse>> cancelGoal(
            @PathVariable("id") Long id,
            @RequestParam("fundId") Long fundId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Saving goal cancelled successfully", 
                savingGoalService.cancelGoal(id, fundId, userId)));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Cannot authenticate current user");
    }
}
