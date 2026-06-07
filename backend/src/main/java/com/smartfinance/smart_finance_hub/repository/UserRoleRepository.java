package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * Query riêng biệt lấy danh sách permission name — tránh MultipleBagFetchException.
     * Lọc các role chưa hết hạn.
     */
    @Query("""
        SELECT DISTINCT p.name
        FROM UserRole ur
        JOIN ur.role r
        JOIN r.rolePermissions rp
        JOIN rp.permission p
        WHERE ur.user.email = :email
          AND (ur.expiredAt IS NULL OR ur.expiredAt > CURRENT_TIMESTAMP)
    """)
    List<String> findPermissionNamesByEmail(@Param("email") String email);
}
