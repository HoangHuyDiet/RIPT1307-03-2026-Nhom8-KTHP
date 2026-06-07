package com.smartfinance.smart_finance_hub.security;

import com.smartfinance.smart_finance_hub.entity.Role;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    @DisplayName("Build cơ bản — nạp roles từ User")
    void shouldBuildWithRolesFromUser() {
        User user = createTestUser();
        CustomUserDetails details = CustomUserDetails.build(user);

        assertEquals("test@test.com", details.getUsername());
        assertEquals(1L, details.getId());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Build overload — nạp roles + permissions riêng biệt")
    void shouldBuildWithRolesAndPermissions() {
        User user = createTestUser();
        List<String> permissions = List.of("CONSULTATION_VIEW_QUEUE", "RAG_REBUILD");

        CustomUserDetails details = CustomUserDetails.build(user, permissions);

        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();

        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("CONSULTATION_VIEW_QUEUE")));
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("RAG_REBUILD")));
    }

    @Test
    @DisplayName("Lọc role hết hạn trong build overload")
    void shouldFilterExpiredRoles() {
        User user = createTestUser();

        Role expiredRole = Role.builder().id(2L).name("SUPPORT_ADMIN").build();
        UserRole expiredUserRole = UserRole.builder()
            .user(user)
            .role(expiredRole)
            .expiredAt(LocalDateTime.now().minusDays(1)) // Đã hết hạn
            .build();
        user.getUserRoles().add(expiredUserRole);

        CustomUserDetails details = CustomUserDetails.build(user, List.of());

        assertFalse(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPORT_ADMIN")));
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Permissions trùng lặp chỉ xuất hiện 1 lần")
    void shouldDeduplicatePermissions() {
        User user = createTestUser();
        List<String> permissions = List.of("RAG_REBUILD", "RAG_REBUILD", "RAG_REBUILD");

        CustomUserDetails details = CustomUserDetails.build(user, permissions);

        long count = details.getAuthorities().stream()
            .filter(a -> a.getAuthority().equals("RAG_REBUILD"))
            .count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Build với null permissions — không crash")
    void shouldHandleNullPermissions() {
        User user = createTestUser();
        CustomUserDetails details = CustomUserDetails.build(user, null);
        assertNotNull(details);
        assertFalse(details.getAuthorities().isEmpty()); 
    }

    private User createTestUser() {
        Role userRole = Role.builder().id(1L).name("USER").build();
        UserRole ur = UserRole.builder()
            .user(null)
            .role(userRole)
            .expiredAt(null) 
            .build();

        User user = User.builder()
            .id(1L)
            .email("test@test.com")
            .password("hashed")
            .build();
        user.setUserRoles(new java.util.ArrayList<>(List.of(ur)));
        ur.setUser(user);

        return user;
    }
}
