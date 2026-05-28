package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.GoalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalTransactionRepository extends JpaRepository<GoalTransaction, Long> {
    List<GoalTransaction> findBySavingGoalIdOrderByTransactionDateDesc(Long savingGoalId);
}
