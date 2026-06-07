package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.LockRequest;
import com.smartfinance.smart_finance_hub.enums.LockRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LockRequestRepository extends JpaRepository<LockRequest, Long> {

    List<LockRequest> findAllByOrderByCreatedAtDesc();

    List<LockRequest> findByStatusOrderByCreatedAtDesc(LockRequestStatus status);
}
