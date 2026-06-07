package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.SubscriptionPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByCode(String code);

    List<SubscriptionPlan> findByActiveTrueOrderByPriceAsc();
}
