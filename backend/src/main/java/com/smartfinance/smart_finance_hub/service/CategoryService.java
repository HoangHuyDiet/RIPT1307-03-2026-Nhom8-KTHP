package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.CreateCategoryRequest;
import com.smartfinance.smart_finance_hub.dto.request.UpdateCategoryRequest;
import com.smartfinance.smart_finance_hub.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getCategories(Long userId, String type);

    CategoryResponse createCategory(Long userId, CreateCategoryRequest request);

    CategoryResponse updateCategory(Long userId, Long categoryId, UpdateCategoryRequest request);

    void deleteCategory(Long userId, Long categoryId);

    List<CategoryResponse> getSystemCategories(String type);

    CategoryResponse createSystemCategory(CreateCategoryRequest request);

    CategoryResponse updateSystemCategory(Long categoryId, UpdateCategoryRequest request);

    void deleteSystemCategory(Long categoryId);
}
