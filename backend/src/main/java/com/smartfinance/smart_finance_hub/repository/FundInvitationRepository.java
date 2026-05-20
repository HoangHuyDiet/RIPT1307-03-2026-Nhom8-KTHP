package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundInvitationRepository extends JpaRepository<FundInvitation, Long> {

    List<FundInvitation> findByShareFundId(Long fundId);

    List<FundInvitation> findByInvitedEmail(String email);

    Optional<FundInvitation> findByInvitationToken(String token);

    List<FundInvitation> findByShareFundIdAndStatus(Long fundId, String status);
}
