package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.FundMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FundMessageRepository extends JpaRepository<FundMessage, Long> {

    List<FundMessage> findByFundIdOrderByCreatedAtAsc(Long fundId);
}


