package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.FundMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundMemberRepository extends JpaRepository<FundMember, Long> {

    List<FundMember> findByFundId(Long fundId);

    List<FundMember> findByUserId(Long userId);

    Optional<FundMember> findByFundIdAndUserId(Long fundId, Long userId);

    Optional<FundMember> findByFundIdAndUserEmailIgnoreCase(Long fundId, String email);

    boolean existsByFundIdAndUserId(Long fundId, Long userId);

    boolean existsByFundIdAndUserIdAndFundRole(Long fundId, Long userId, String role);
}


