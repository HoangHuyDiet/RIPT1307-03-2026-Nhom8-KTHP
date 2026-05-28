package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundInvitationRepository extends JpaRepository<FundInvitation, Long> {

    List<FundInvitation> findByFundId(Long fundId);

    List<FundInvitation> findByInvitedEmail(String email);

    Optional<FundInvitation> findByInvitationToken(String token);

    List<FundInvitation> findByFundIdAndStatus(Long fundId, String status);

    boolean existsByFundIdAndInvitedEmailIgnoreCaseAndTypeAndStatus(
            Long fundId, String invitedEmail, String type, String status);

    List<FundInvitation> findByFundIdAndTypeAndStatus(Long fundId, String type, String status);

    List<FundInvitation> findByFundIdAndType(Long fundId, String type);

    List<FundInvitation> findByInvitedEmailIgnoreCaseAndStatus(String email, String status);

    List<FundInvitation> findByFundIdAndInvitedEmailIgnoreCaseAndStatus(
            Long fundId, String invitedEmail, String status);
}


