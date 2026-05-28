package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.SavingGoal;
import com.smartfinance.smart_finance_hub.enums.SavingGoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {

    List<SavingGoal> findByUserIdAndDeletedAtIsNull(Long userId);

    Optional<SavingGoal> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    List<SavingGoal> findByUserIdAndStatusAndDeletedAtIsNull(Long userId, SavingGoalStatus status);
}
