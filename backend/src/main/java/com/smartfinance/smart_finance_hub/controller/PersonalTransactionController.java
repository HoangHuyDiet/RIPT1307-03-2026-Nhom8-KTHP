package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.CreateTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.dto.response.TransactionResponse;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.PersonalTransactionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PersonalTransactionController {

    private final PersonalTransactionService personalTransactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @Valid @RequestBody CreateTransactionRequest request) {
        Long userId = getCurrentUserId();
        TransactionResponse data = personalTransactionService.createTransaction(request, userId);
        return new ResponseEntity<>(
                ApiResponse.success("Tạo giao dịch thành công!", data), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("date").descending());

        Page<TransactionResponse> data =
                personalTransactionService.getTransactions(userId, type, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách giao dịch thành công!", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(
            @PathVariable("id") Long transactionId) {
        Long userId = getCurrentUserId();
        TransactionResponse data = personalTransactionService.getTransactionById(transactionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết giao dịch thành công!", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @PathVariable("id") Long transactionId,
            @Valid @RequestBody UpdateTransactionRequest request) {
        Long userId = getCurrentUserId();
        TransactionResponse data =
                personalTransactionService.updateTransaction(transactionId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật giao dịch thành công!", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("id") Long transactionId) {
        Long userId = getCurrentUserId();
        personalTransactionService.deleteTransaction(transactionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa giao dịch thành công!"));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Không thể xác thực người dùng hiện tại!");
    }
}
