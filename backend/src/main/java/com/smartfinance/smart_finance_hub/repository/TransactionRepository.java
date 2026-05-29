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

    List<Transaction> findByShareFundId(Long fundId);

    Page<Transaction> findByShareFundId(Long fundId, Pageable pageable);

    Page<Transaction> findByShareFundIdAndType(Long fundId, String type, Pageable pageable);

    List<Transaction> findByShareFundIdAndIsApproved(Long fundId, Boolean isApproved);

    List<Transaction> findByShareFundIdAndStatus(Long fundId, String status);

    List<Transaction> findByShareFundIdInAndStatus(List<Long> fundIds, String status);

    long countByShareFundId(Long fundId);

    long countByShareFundIdAndIsApproved(Long fundId, Boolean isApproved);

    List<Transaction> findByShareFundIdAndIsApprovedAndDateBetween(
            Long fundId, Boolean isApproved, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByShareFundIdInAndIsApprovedAndDateBetween(
            List<Long> fundIds, Boolean isApproved, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByCategoryId(Long categoryId);

   
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Page<Transaction> findByUserIdAndShareFundIsNull(Long userId, Pageable pageable);

    Page<Transaction> findByUserIdAndTypeAndShareFundIsNull(
            Long userId, String type, Pageable pageable);

    Page<Transaction> findByUserIdAndDateBetweenAndShareFundIsNull(
            Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Transaction> findByUserIdAndTypeAndDateBetweenAndShareFundIsNull(
            Long userId, String type, LocalDate startDate, LocalDate endDate, Pageable pageable);
}


