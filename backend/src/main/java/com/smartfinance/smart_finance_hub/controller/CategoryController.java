package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.response.ApiResponse;
import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        // Trả về tất cả danh mục của hệ thống (user_id is null) hoặc của người dùng hiện tại
        Long userId = getCurrentUserId();
        // Giả sử có hàm lấy theo userId hoặc null (system categories). Nếu không có, tạm lấy tất cả.
        // Tối ưu nhất: lấy tất cả categories cho đơn giản trong prototype
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục thành công!", categories));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }
}
