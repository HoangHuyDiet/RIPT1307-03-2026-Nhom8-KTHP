package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.ShareFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareFundRepository extends JpaRepository<ShareFund, Long> {

    List<ShareFund> findByCreatedByUserId(Long userId);

    List<ShareFund> findByStatus(String status);
}
