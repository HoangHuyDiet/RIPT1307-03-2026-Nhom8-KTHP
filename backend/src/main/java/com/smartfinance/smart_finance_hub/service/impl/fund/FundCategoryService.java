package com.smartfinance.smart_finance_hub.service.impl.fund;

import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.TransactionType;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundCategoryService {

    private final CategoryRepository categoryRepository;

    public Category createFundCategory(User user, TransactionType type, String name) {
        Category category = Category.builder()
                .user(user)
                .name(name)
                .type(type.name())
                .description("Auto-created category for fund")
                .build();
        return categoryRepository.save(category);
    }
}
