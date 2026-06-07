package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndType(Long userId, String type);

    List<Transaction> findByShareFundId(Long fundId);

    @Query("""
            select t from Transaction t
            left join fetch t.user
            left join fetch t.category
            where t.shareFund.id = :fundId
            order by t.date desc, t.id desc
            """)
    List<Transaction> findByShareFundIdWithDetails(@Param("fundId") Long fundId);

    Page<Transaction> findByShareFundId(Long fundId, Pageable pageable);

    Page<Transaction> findByShareFundIdAndType(Long fundId, String type, Pageable pageable);

    List<Transaction> findByShareFundIdAndIsApproved(Long fundId, Boolean isApproved);

    List<Transaction> findByShareFundIdAndStatus(Long fundId, String status);

    List<Transaction> findByShareFundIdInAndStatus(List<Long> fundIds, String status);

    @Query("""
            select t from Transaction t
            left join fetch t.shareFund
            left join fetch t.user
            where t.user.id = :userId
              and t.status = :status
              and t.shareFund is not null
            order by t.updatedAt desc, t.id desc
            """)
    List<Transaction> findSharedFundTransactionsByUserIdAndStatusWithDetails(
            @Param("userId") Long userId,
            @Param("status") String status);

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

    @Query("""
            select t from Transaction t
            left join t.category c
            left join t.personalFund pf
            where t.user.id = :userId
              and t.shareFund is null
              and (:type is null or t.type = :type)
              and (:categoryId is null or c.id = :categoryId)
              and (:personalFundId is null or pf.id = :personalFundId)
              and (:startDate is null or t.date >= :startDate)
              and (:endDate is null or t.date <= :endDate)
              and (:search is null or lower(t.description) like lower(concat('%', :search, '%')))
            """)
    Page<Transaction> searchPersonalTransactions(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("categoryId") Long categoryId,
            @Param("personalFundId") Long personalFundId,
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    Page<Transaction> findByPersonalFundId(Long personalFundId, Pageable pageable);

    List<Transaction> findByPersonalFundIdAndDateBetweenOrderByDateAsc(
            Long personalFundId, LocalDate startDate, LocalDate endDate);

    Page<Transaction> findByPersonalFundIdAndType(Long personalFundId, String type, Pageable pageable);

    @Query("SELECT new com.smartfinance.smart_finance_hub.dto.response.CategoryExpenseResponse(c.name, SUM(t.amount)) " +
           "FROM Transaction t JOIN t.category c " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.isApproved = true " +
           "AND FUNCTION('MONTH', t.date) = :month AND FUNCTION('YEAR', t.date) = :year " +
           "GROUP BY c.name")
    List<com.smartfinance.smart_finance_hub.dto.response.CategoryExpenseResponse> getExpenseByCategory(
            @Param("userId") Long userId, 
            @Param("month") int month, 
            @Param("year") int year);

    @Query("SELECT new com.smartfinance.smart_finance_hub.dto.response.CashFlowResponse(FUNCTION('MONTH', t.date), t.type, SUM(t.amount)) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.isApproved = true AND FUNCTION('YEAR', t.date) = :year " +
           "GROUP BY FUNCTION('MONTH', t.date), t.type")
    List<com.smartfinance.smart_finance_hub.dto.response.CashFlowResponse> getCashFlowByYear(
            @Param("userId") Long userId, 
            @Param("year") int year);
}


