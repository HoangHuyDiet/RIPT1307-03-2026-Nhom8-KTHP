package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.PersonalFund;
import com.smartfinance.smart_finance_hub.enums.FundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalFundRepository extends JpaRepository<PersonalFund, Long> {

    List<PersonalFund> findByUserIdAndStatusNot(Long userId, FundStatus status);

    List<PersonalFund> findByUserIdAndStatus(Long userId, FundStatus status);

    Optional<PersonalFund> findByIdAndUserId(Long id, Long userId);

    List<PersonalFund> findByUserId(Long userId);
}
