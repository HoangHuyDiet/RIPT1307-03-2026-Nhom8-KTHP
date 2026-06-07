package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.AdminCreateUserRequest;
import com.smartfinance.smart_finance_hub.dto.request.AdminUpdateUserRequest;
import com.smartfinance.smart_finance_hub.dto.response.AdminDashboardStats;
import com.smartfinance.smart_finance_hub.dto.response.AdminUserResponse;
import com.smartfinance.smart_finance_hub.entity.Role;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.entity.UserRole;
import com.smartfinance.smart_finance_hub.enums.UserStatus;
import com.smartfinance.smart_finance_hub.repository.RoleRepository;
import com.smartfinance.smart_finance_hub.repository.TransactionRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.repository.UserRoleRepository;
import com.smartfinance.smart_finance_hub.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public List<AdminUserResponse> getAllUsers() {
        log.info("Admin: Lấy danh sách tất cả user");
        List<User> users = userRepository.findAllWithRoles();

        return users.stream()
            .map(this::mapToAdminUserResponse)
            .collect(Collectors.toList());
    }

    @Override
    public AdminUserResponse getUserById(Long userId) {
        log.info("Admin: Lấy thông tin user id={}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với id: " + userId));

        User userWithRoles = userRepository.findByEmailWithRoles(user.getEmail())
            .orElseThrow(() -> new RuntimeException("Lỗi hệ thống"));

        return mapToAdminUserResponse(userWithRoles);
    }


    @Override
    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest request) {
        log.info("Admin: Tạo user mới với email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + request.getEmail());
        }

        UserStatus status = UserStatus.ACTIVE;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = UserStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ: " + request.getStatus());
            }
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .displayName(request.getDisplayName())
            .status(status)
            .build();
        userRepository.save(user);

        List<String> roleNames = (request.getRoles() != null && !request.getRoles().isEmpty())
            ? request.getRoles()
            : List.of("USER");

        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Role không tồn tại: " + roleName));
            UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();
            userRoleRepository.save(userRole);
        }

        log.info("Đã tạo user {} với roles {}", request.getEmail(), roleNames);

        User createdUser = userRepository.findByEmailWithRoles(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Lỗi hệ thống"));

        return mapToAdminUserResponse(createdUser);
    }


    @Override
    @Transactional
    public AdminUserResponse updateUser(Long userId, AdminUpdateUserRequest request) {
        log.info("Admin: Cập nhật user id={}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với id: " + userId));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                user.setStatus(UserStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ: " + request.getStatus());
            }
        }

        userRepository.save(user);

        if (request.getRoles() != null) {
            List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
            userRoleRepository.deleteAll(existingRoles);

            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName.toUpperCase())
                    .orElseThrow(() -> new IllegalArgumentException("Role không tồn tại: " + roleName));
                UserRole userRole = UserRole.builder()
                    .user(user)
                    .role(role)
                    .build();
                userRoleRepository.save(userRole);
            }
            log.info("Đã cập nhật roles cho user {} thành {}", userId, request.getRoles());
        }

        log.info("Đã cập nhật user id={}", userId);

        User updatedUser = userRepository.findByEmailWithRoles(user.getEmail())
            .orElseThrow(() -> new RuntimeException("Lỗi hệ thống"));

        return mapToAdminUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(Long userId, String status) {
        log.info("Admin: Cập nhật status user {} thành {}", userId, status);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với id: " + userId));

        try {
            UserStatus newStatus = UserStatus.valueOf(status.toUpperCase());
            user.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status + ". Cho phép: ACTIVE, INACTIVE, BANNED");
        }

        userRepository.save(user);
        log.info("Đã cập nhật status user {} thành {}", userId, status);

        User updatedUser = userRepository.findByEmailWithRoles(user.getEmail())
            .orElseThrow(() -> new RuntimeException("Lỗi hệ thống"));

        return mapToAdminUserResponse(updatedUser);
    }


    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Admin: Xóa user id={}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với id: " + userId));

        User userWithRoles = userRepository.findByEmailWithRoles(user.getEmail())
            .orElseThrow(() -> new RuntimeException("Lỗi hệ thống"));

        boolean isAdmin = userWithRoles.getUserRoles() != null &&
            userWithRoles.getUserRoles().stream()
                .anyMatch(ur -> "ADMIN".equals(ur.getRole().getName()));

        if (isAdmin) {
            throw new IllegalArgumentException("Không thể xóa tài khoản Admin!");
        }

        List<UserRole> roles = userRoleRepository.findByUserId(userId);
        userRoleRepository.deleteAll(roles);

        userRepository.delete(user);
        log.info("Đã xóa user id={}, email={}", userId, user.getEmail());
    }


    @Override
    public AdminDashboardStats getDashboardStats() {
        log.info("Admin: Lấy thống kê dashboard");

        long totalUsers = userRepository.count();
        long totalTransactions = transactionRepository.count();

        List<User> allUsers = userRepository.findAll();
        long activeUsers = allUsers.stream()
            .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))
            .count();
        long bannedUsers = allUsers.stream()
            .filter(u -> UserStatus.BANNED.equals(u.getStatus()))
            .count();

        return AdminDashboardStats.builder()
            .totalUsers(totalUsers)
            .totalTransactions(totalTransactions)
            .activeUsers(activeUsers)
            .bannedUsers(bannedUsers)
            .build();
    }


    private AdminUserResponse mapToAdminUserResponse(User user) {
        List<String> roleNames = (user.getUserRoles() != null)
            ? user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList())
            : Collections.emptyList();

        return AdminUserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .displayName(user.getDisplayName())
            .status(user.getStatus().name())
            .roles(roleNames)
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
