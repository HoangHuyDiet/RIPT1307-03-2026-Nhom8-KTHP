package com.smartfinance.smart_finance_hub.repository;

import com.smartfinance.smart_finance_hub.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdAndType(Long userId, String type);

    List<Category> findByUserIdAndDeletedAtIsNull(Long userId);

    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
            select c from Category c
            where c.deletedAt is null
              and (c.user is null or c.user.id = :userId)
              and (:type is null or c.type = :type)
            order by c.type asc, c.name asc
            """)
    List<Category> findAvailableCategories(@Param("userId") Long userId, @Param("type") String type);

    @Query("""
            select case when count(c) > 0 then true else false end
            from Category c
            where c.deletedAt is null
              and (c.user is null or c.user.id = :userId)
              and lower(c.name) = lower(:name)
              and c.type = :type
            """)
    boolean existsActiveCategoryNameInUserScope(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("type") String type);

    @Query("""
            select c from Category c
            where c.deletedAt is null
              and c.user is null
              and (:type is null or c.type = :type)
            order by c.type asc, c.name asc
            """)
    List<Category> findSystemCategories(@Param("type") String type);

    @Query("""
            select case when count(c) > 0 then true else false end
            from Category c
            where c.deletedAt is null
              and c.user is null
              and lower(c.name) = lower(:name)
              and c.type = :type
            """)
    boolean existsActiveSystemCategoryName(
            @Param("name") String name,
            @Param("type") String type);
}
