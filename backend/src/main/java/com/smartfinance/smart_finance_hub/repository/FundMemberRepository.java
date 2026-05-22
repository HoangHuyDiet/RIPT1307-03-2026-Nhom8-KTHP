package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.FundMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundMemberRepository extends JpaRepository<FundMember, Long> {

    List<FundMember> findByShareFundId(Long fundId);

    List<FundMember> findByUserId(Long userId);

    Optional<FundMember> findByShareFundIdAndUserId(Long fundId, Long userId);

    boolean existsByShareFundIdAndUserId(Long fundId, Long userId);
}
