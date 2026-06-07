package com.smartfinance.smart_finance_hub.security;

import com.smartfinance.smart_finance_hub.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
  private Long id;
  private String email;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;

  // Giữ backward compatibility cho code hiện tại
  public static CustomUserDetails build(User user) {
    List<GrantedAuthority> authorities = Collections.emptyList();

    if (user.getUserRoles() != null) {
      authorities = user.getUserRoles().stream()
          .filter(ur -> ur.getExpiredAt() == null || ur.getExpiredAt().isAfter(LocalDateTime.now()))
          .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getName()))
          .collect(Collectors.toList());
    }

    return new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        authorities
    );
  }


  /**
   * Build với permissions nạp độc lập từ query riêng — tránh MultipleBagFetchException.
   * Lọc role hết hạn và nạp permissions dạng GrantedAuthority.
   */
  public static CustomUserDetails build(User user, List<String> permissionNames) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    // Nạp Roles (lọc role hết hạn)
    if (user.getUserRoles() != null) {
      user.getUserRoles().stream()
          .filter(ur -> ur.getExpiredAt() == null || ur.getExpiredAt().isAfter(LocalDateTime.now()))
          .forEach(ur -> authorities.add(new SimpleGrantedAuthority("ROLE_" + ur.getRole().getName())));
    }

    // Nạp Permissions (từ query riêng biệt)
    if (permissionNames != null) {
      permissionNames.stream()
          .distinct()
          .forEach(name -> authorities.add(new SimpleGrantedAuthority(name)));
    }

    return new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        authorities
    );
  }

  @Override
  public String getUsername() { return email; }

  @Override
  public boolean isAccountNonExpired() { return true; }
  @Override
  public boolean isAccountNonLocked() { return true; }
  @Override
  public boolean isCredentialsNonExpired() { return true; }
  @Override
  public boolean isEnabled() { return true; }
}
