package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.CreateCategoryRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateCategoryRequest;
import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.dto.response.CategoryResponse;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @RequestParam(required = false) String type) {
        Long userId = getCurrentUserId();
        List<CategoryResponse> categories = categoryService.getCategories(userId, type);
        return ResponseEntity.ok(ApiResponse.success("Lay danh muc thanh cong!", categories));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        Long userId = getCurrentUserId();
        CategoryResponse category = categoryService.createCategory(userId, request);
        return new ResponseEntity<>(
                ApiResponse.success("Tao danh muc thanh cong!", category), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable("id") Long categoryId,
            @RequestBody UpdateCategoryRequest request) {
        Long userId = getCurrentUserId();
        CategoryResponse category = categoryService.updateCategory(userId, categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat danh muc thanh cong!", category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable("id") Long categoryId) {
        Long userId = getCurrentUserId();
        categoryService.deleteCategory(userId, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Xoa danh muc thanh cong!"));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("Khong the xac thuc nguoi dung hien tai!");
    }
}
