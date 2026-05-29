package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.FundActivity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FundActivityRepository extends JpaRepository<FundActivity, Long> {

    List<FundActivity> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    List<FundActivity> findTop20ByShareFundIdOrderByCreatedAtDesc(Long fundId);

    List<FundActivity> findByShareFundId(Long fundId);
}
