package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.CreateCategoryRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateCategoryRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.dto.response.CategoryResponse;
import com.smartfinance.smart_finance_hub.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSystemCategories(
            @RequestParam(required = false) String type) {
        log.info("Admin API: GET /api/admin/categories, type={}", type);
        List<CategoryResponse> categories = categoryService.getSystemCategories(type);
        return ResponseEntity.ok(ApiResponse.success("Lay danh muc he thong thanh cong!", categories));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createSystemCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        log.info("Admin API: POST /api/admin/categories");
        CategoryResponse category = categoryService.createSystemCategory(request);
        return new ResponseEntity<>(
                ApiResponse.success("Tao danh muc he thong thanh cong!", category), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateSystemCategory(
            @PathVariable("id") Long categoryId,
            @RequestBody UpdateCategoryRequest request) {
        log.info("Admin API: PUT /api/admin/categories/{}", categoryId);
        CategoryResponse category = categoryService.updateSystemCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat danh muc he thong thanh cong!", category));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSystemCategory(@PathVariable("id") Long categoryId) {
        log.info("Admin API: DELETE /api/admin/categories/{}", categoryId);
        categoryService.deleteSystemCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Xoa danh muc he thong thanh cong!"));
    }
}
