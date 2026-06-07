package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.dto.response.CashFlowResponse;
import com.smartfinance.smart_finance_hub.dto.response.CategoryExpenseResponse;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/expense-by-category")
    public ResponseEntity<ApiResponse<List<CategoryExpenseResponse>>> getExpenseByCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int month,
            @RequestParam int year) {
        log.info("Lấy thống kê chi tiêu theo danh mục cho user {}, tháng {}/{}", userDetails.getId(), month, year);
        List<CategoryExpenseResponse> data = dashboardService.getExpenseByCategory(userDetails.getId(), month, year);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", data));
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<ApiResponse<List<CashFlowResponse>>> getCashFlowByYear(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year) {
        log.info("Lấy thống kê dòng tiền cho user {}, năm {}", userDetails.getId(), year);
        List<CashFlowResponse> data = dashboardService.getCashFlowByYear(userDetails.getId(), year);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", data));
    }
}
