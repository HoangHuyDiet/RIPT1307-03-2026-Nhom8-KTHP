package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.RecurringSetting;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurringSettingRepository extends JpaRepository<RecurringSetting, Long> {

    List<RecurringSetting> findByUserId(Long userId);

    List<RecurringSetting> findByUserIdAndIsActive(Long userId, Boolean isActive);

    @Query("select r.id from RecurringSetting r where r.isActive = true and r.nextRunDate <= :date")
    List<Long> findDueSettingIds(@Param("date") LocalDate date);

    @Query("""
            select r from RecurringSetting r
            join fetch r.user
            join fetch r.category
            where r.id = :id
            """)
    Optional<RecurringSetting> findByIdWithRelations(@Param("id") Long id);
}
