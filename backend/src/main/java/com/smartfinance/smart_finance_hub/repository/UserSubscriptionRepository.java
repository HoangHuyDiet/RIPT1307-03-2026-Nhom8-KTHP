package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.UserSubscription;
import com.smartfinance.smart_finance_hub.enums.SubscriptionStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    Optional<UserSubscription> findFirstByUserIdAndStatusAndExpiredAtAfterOrderByExpiredAtDesc(
        Long userId,
        SubscriptionStatus status,
        LocalDateTime now
    );
}
