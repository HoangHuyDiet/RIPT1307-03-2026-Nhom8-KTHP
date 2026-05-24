package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndType(Long userId, String type);

    List<Transaction> findByShareFundId(Long fundId);

    List<Transaction> findByShareFundIdAndIsApproved(Long fundId, Boolean isApproved);

    List<Transaction> findByCategoryId(Long categoryId);

   
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
