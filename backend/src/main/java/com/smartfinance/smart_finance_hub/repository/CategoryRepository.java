package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdAndType(Long userId, String type);

    List<Category> findByUserIdAndDeletedAtIsNull(Long userId);
}
