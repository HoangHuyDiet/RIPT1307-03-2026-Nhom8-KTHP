package com.smartfinance.smart_finance_hub.security.impl;

import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.repository.UserRoleRepository;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.info("Bắt đầu truy vấn thông tin User từ Database với email: {}", email);

    User user = userRepository.findByEmailWithRoles(email)
        .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

    // Query permissions riêng biệt — tránh MultipleBagFetchException
    List<String> permissions = userRoleRepository.findPermissionNamesByEmail(email);

    return CustomUserDetails.build(user, permissions);
  }
}