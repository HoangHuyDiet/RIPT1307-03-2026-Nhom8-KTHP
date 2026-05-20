package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.MonthlyStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyStatementRepository extends JpaRepository<MonthlyStatement, Long> {

    List<MonthlyStatement> findByUserId(Long userId);

    Optional<MonthlyStatement> findByUserIdAndMonth(Long userId, String month);
}
