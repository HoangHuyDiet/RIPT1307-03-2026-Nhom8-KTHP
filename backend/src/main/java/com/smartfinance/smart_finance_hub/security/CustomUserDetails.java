package com.smartfinance.smart_finance_hub.security;

import com.smartfinance.smart_finance_hub.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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

  public static CustomUserDetails build(User user) {
    List<GrantedAuthority> authorities = Collections.emptyList();

    if (user.getUserRoles() != null) {
      authorities = user.getUserRoles().stream()
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
