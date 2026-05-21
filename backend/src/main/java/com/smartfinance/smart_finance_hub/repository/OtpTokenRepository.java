package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndIsUsedFalseOrderByIdDesc(String email);

    void deleteByEmail(String email);
}
