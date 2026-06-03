package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.CreatePersonalFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.CreatePersonalFundRequestWrapper;
import com.smartfinance.smart_finance_hub.dto.request.InternalTransferRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdatePersonalFundRequest;
import com.smartfinance.smart_finance_hub.dto.request.PayBillRequest;
import com.smartfinance.smart_finance_hub.dto.request.DepositRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.dto.response.AssetAllocationResponse;
import com.smartfinance.smart_finance_hub.dto.response.FundBalanceHistoryResponse;
import com.smartfinance.smart_finance_hub.dto.response.PersonalFundResponse;
import com.smartfinance.smart_finance_hub.dto.response.PersonalFundSummaryResponse;
import com.smartfinance.smart_finance_hub.dto.response.TransactionResponse;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.PersonalFundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/personal-funds")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PersonalFundController {

    private final PersonalFundService personalFundService;

    @PostMapping
    public ResponseEntity<ApiResponse<PersonalFundResponse>> createFund(
            @Valid @RequestBody CreatePersonalFundRequest request) {
        Long userId = getCurrentUserId();
        PersonalFundResponse data = personalFundService.createFund(userId, request);
        return new ResponseEntity<>(
                ApiResponse.success("Tạo quỹ cá nhân thành công!", data), HttpStatus.CREATED);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PersonalFundResponse>> createFundAlt(
            @RequestBody CreatePersonalFundRequestWrapper requestWrapper) {
        Long userId = getCurrentUserId();
        String fundType = requestWrapper.getFundType();
        if (fundType == null || fundType.isBlank()) {
            String icon = requestWrapper.getIcon();
            if ("bank".equalsIgnoreCase(icon)) fundType = "BANK_ACCOUNT";
            else if ("card".equalsIgnoreCase(icon)) fundType = "CREDIT_CARD";
            else if ("mobile".equalsIgnoreCase(icon)) fundType = "E_WALLET";
            else if ("dollar".equalsIgnoreCase(icon)) fundType = "INVESTMENT";
            else fundType = "CASH";
        }
        BigDecimal initialBal = requestWrapper.getInitialBalance() != null ? requestWrapper.getInitialBalance() : requestWrapper.getBalance();
        
        CreatePersonalFundRequest request = new CreatePersonalFundRequest(
                requestWrapper.getName(),
                fundType,
                initialBal != null ? initialBal : BigDecimal.ZERO,
                requestWrapper.getCurrency() != null ? requestWrapper.getCurrency() : "VND",
                requestWrapper.getDescription()
        );
        PersonalFundResponse data = personalFundService.createFund(userId, request);
        return new ResponseEntity<>(
                ApiResponse.success("Tạo quỹ cá nhân thành công!", data), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PersonalFundResponse>>> getAllFunds() {
        Long userId = getCurrentUserId();
        List<PersonalFundResponse> data = personalFundService.getAllFunds(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách quỹ cá nhân thành công!", data));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<PersonalFundResponse>>> getListFunds() {
        return getAllFunds();
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PersonalFundSummaryResponse>> getSummary() {
        Long userId = getCurrentUserId();
        PersonalFundSummaryResponse data = personalFundService.getSummary(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy tóm tắt quỹ thành công!", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonalFundResponse>> getFundById(
            @PathVariable("id") Long fundId) {
        Long userId = getCurrentUserId();
        PersonalFundResponse data = personalFundService.getFundById(userId, fundId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin quỹ thành công!", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonalFundResponse>> updateFund(
            @PathVariable("id") Long fundId,
            @Valid @RequestBody UpdatePersonalFundRequest request) {
        Long userId = getCurrentUserId();
        PersonalFundResponse data = personalFundService.updateFund(userId, fundId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật quỹ thành công!", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> closeFund(@PathVariable("id") Long fundId) {
        Long userId = getCurrentUserId();
        personalFundService.closeFund(userId, fundId);
        return ResponseEntity.ok(ApiResponse.success("Đóng quỹ thành công!"));
    }

    @GetMapping("/total-assets")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalAssets() {
        Long userId = getCurrentUserId();
        BigDecimal total = personalFundService.getTotalAssets(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy tổng tài sản thành công!", total));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Void>> internalTransfer(
            @Valid @RequestBody InternalTransferRequest request) {
        Long userId = getCurrentUserId();
        personalFundService.internalTransfer(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Chuyển tiền nội bộ thành công!"));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getFundTransactions(
            @PathVariable("id") Long fundId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("date").descending());

        Page<TransactionResponse> data = personalFundService.getFundTransactions(userId, fundId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử giao dịch quỹ thành công!", data));
    }

    @GetMapping("/reports/allocation")
    public ResponseEntity<ApiResponse<List<AssetAllocationResponse>>> getAssetAllocation() {
        Long userId = getCurrentUserId();
        List<AssetAllocationResponse> data = personalFundService.getAssetAllocation(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy phân bổ tài sản thành công!", data));
    }

    @GetMapping("/distribution")
    public ResponseEntity<ApiResponse<List<AssetAllocationResponse>>> getDistribution() {
        return getAssetAllocation();
    }

    @GetMapping("/{id}/reports/balance-history")
    public ResponseEntity<ApiResponse<List<FundBalanceHistoryResponse>>> getFundBalanceHistory(
            @PathVariable("id") Long fundId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = getCurrentUserId();
        List<FundBalanceHistoryResponse> data =
                personalFundService.getFundBalanceHistory(userId, fundId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy biến động số dư quỹ thành công!", data));
    }

    @GetMapping("/balance-history")
    public ResponseEntity<ApiResponse<List<FundBalanceHistoryResponse>>> getGlobalBalanceHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = getCurrentUserId();
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        List<FundBalanceHistoryResponse> data = personalFundService.getGlobalBalanceHistory(userId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Lấy biến động số dư tài sản thành công!", data));
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getRecentTransactions() {
        Long userId = getCurrentUserId();
        List<TransactionResponse> data = personalFundService.getRecentTransactions(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy giao dịch gần đây thành công!", data));
    }

    @PostMapping("/pay-bill")
    public ResponseEntity<ApiResponse<Void>> payBill(@RequestBody PayBillRequest request) {
        Long userId = getCurrentUserId();
        personalFundService.payBill(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Thanh toán hóa đơn thành công!"));
    }

    @PostMapping("/reminder")
    public ResponseEntity<ApiResponse<Void>> reminder() {
        return ResponseEntity.ok(ApiResponse.success("Lên lịch nhắc thanh toán thành công!"));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<ApiResponse<Void>> deposit(
            @PathVariable("id") Long fundId,
            @RequestBody DepositRequest request) {
        Long userId = getCurrentUserId();
        personalFundService.deposit(
                userId,
                fundId,
                request.getAmount(),
                request.getDescription(),
                request.getCategoryId(),
                request.getDate());
        return ResponseEntity.ok(ApiResponse.success("Nạp tiền vào quỹ thành công!"));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Không thể xác thực người dùng hiện tại!");
    }
}
