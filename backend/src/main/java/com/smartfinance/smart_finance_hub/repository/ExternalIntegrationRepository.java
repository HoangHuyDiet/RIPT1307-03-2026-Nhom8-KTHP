package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.ExternalIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalIntegrationRepository extends JpaRepository<ExternalIntegration, Long> {

    List<ExternalIntegration> findByUserId(Long userId);

    List<ExternalIntegration> findByUserIdAndIsActive(Long userId, Boolean isActive);

    List<ExternalIntegration> findByProviderName(String providerName);
}
