package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.SavingGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {

    List<SavingGoal> findByUserId(Long userId);

    List<SavingGoal> findByUserIdAndStatus(Long userId, String status);
}


