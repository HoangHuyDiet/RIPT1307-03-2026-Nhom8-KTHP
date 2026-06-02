package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.CreateCategoryRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateCategoryRequest;
import com.smartfinance.smart_finance_hub.dto.response.CategoryResponse;
import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long userId, String type) {
        String normalizedType = normalizeTypeOrNull(type);
        return categoryRepository.findAvailableCategories(userId, normalizedType)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(Long userId, CreateCategoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi dung!"));

        String name = normalizeName(request.getName());
        String type = normalizeType(request.getType());

        if (categoryRepository.existsActiveCategoryNameInUserScope(userId, name, type)) {
            throw new IllegalArgumentException("Danh muc da ton tai: " + name);
        }

        Category category = Category.builder()
                .user(user)
                .name(name)
                .type(type)
                .description(request.getDescription())
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long userId, Long categoryId, UpdateCategoryRequest request) {
        Category category = findEditableUserCategory(userId, categoryId);

        if (request.getName() != null && !request.getName().isBlank()) {
            String name = normalizeName(request.getName());
            if (!name.equalsIgnoreCase(category.getName())
                    && categoryRepository.existsActiveCategoryNameInUserScope(userId, name, category.getType())) {
                throw new IllegalArgumentException("Danh muc da ton tai: " + name);
            }
            category.setName(name);
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = findEditableUserCategory(userId, categoryId);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSystemCategories(String type) {
        String normalizedType = normalizeTypeOrNull(type);
        return categoryRepository.findSystemCategories(normalizedType)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createSystemCategory(CreateCategoryRequest request) {
        String name = normalizeName(request.getName());
        String type = normalizeType(request.getType());

        if (categoryRepository.existsActiveSystemCategoryName(name, type)) {
            throw new IllegalArgumentException("Danh muc he thong da ton tai: " + name);
        }

        Category category = Category.builder()
                .user(null)
                .name(name)
                .type(type)
                .description(request.getDescription())
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateSystemCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = findEditableSystemCategory(categoryId);

        if (request.getName() != null && !request.getName().isBlank()) {
            String name = normalizeName(request.getName());
            if (!name.equalsIgnoreCase(category.getName())
                    && categoryRepository.existsActiveSystemCategoryName(name, category.getType())) {
                throw new IllegalArgumentException("Danh muc he thong da ton tai: " + name);
            }
            category.setName(name);
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteSystemCategory(Long categoryId) {
        Category category = findEditableSystemCategory(categoryId);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    private Category findEditableUserCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay danh muc!"));

        if (category.getUser() == null) {
            throw new IllegalArgumentException("Khong the sua hoac xoa danh muc he thong!");
        }
        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Ban khong co quyen thao tac danh muc nay!");
        }
        return category;
    }

    private Category findEditableSystemCategory(Long categoryId) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay danh muc he thong!"));

        if (category.getUser() != null) {
            throw new IllegalArgumentException("Endpoint admin chi thao tac danh muc he thong!");
        }
        return category;
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Ten danh muc khong duoc de trong!");
        }
        return normalized;
    }

    private String normalizeType(String type) {
        String normalized = normalizeTypeOrNull(type);
        if (normalized == null) {
            throw new IllegalArgumentException("Loai danh muc khong hop le! Chi chap nhan INCOME hoac EXPENSE");
        }
        return normalized;
    }

    private String normalizeTypeOrNull(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        String normalized = type.trim().toUpperCase();
        if (!"INCOME".equals(normalized) && !"EXPENSE".equals(normalized)) {
            throw new IllegalArgumentException("Loai danh muc khong hop le! Chi chap nhan INCOME hoac EXPENSE");
        }
        return normalized;
    }
}
