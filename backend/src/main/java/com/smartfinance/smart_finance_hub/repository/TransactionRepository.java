package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndType(Long userId, String type);

    List<Transaction> findByFundId(Long fundId);

    Page<Transaction> findByFundId(Long fundId, Pageable pageable);

    Page<Transaction> findByFundIdAndType(Long fundId, String type, Pageable pageable);

    List<Transaction> findByFundIdAndIsApproved(Long fundId, Boolean isApproved);

    long countByFundId(Long fundId);

    long countByFundIdAndIsApproved(Long fundId, Boolean isApproved);

    List<Transaction> findByFundIdAndIsApprovedAndDateBetween(
            Long fundId, Boolean isApproved, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByFundIdInAndIsApprovedAndDateBetween(
            List<Long> fundIds, Boolean isApproved, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByCategoryId(Long categoryId);

   
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Page<Transaction> findByUserIdAndFundIsNull(Long userId, Pageable pageable);

    Page<Transaction> findByUserIdAndTypeAndFundIsNull(
            Long userId, String type, Pageable pageable);

    Page<Transaction> findByUserIdAndDateBetweenAndFundIsNull(
            Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Transaction> findByUserIdAndTypeAndDateBetweenAndFundIsNull(
            Long userId, String type, LocalDate startDate, LocalDate endDate, Pageable pageable);
}


