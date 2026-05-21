package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.*;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.exception.business.UserAlreadyExistsException;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("registerUser failed, email is already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email này đã được sử dụng!");
        }

        log.info("registerUser success: {}", request.getEmail());
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setDisplayName(request.getDisplayName());

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        newUser.setPassword(hashedPassword);

        userRepository.save(newUser);
    }
}