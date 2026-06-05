package com.smartfinance.smart_finance_hub.service;

import com.smartfinance.smart_finance_hub.dto.request.AdminCreateUserRequest;
import com.smartfinance.smart_finance_hub.dto.request.AdminUpdateUserRequest;
import com.smartfinance.smart_finance_hub.dto.response.AdminDashboardStats;
import com.smartfinance.smart_finance_hub.dto.response.AdminUserResponse;

import java.util.List;

public interface AdminService {

    List<AdminUserResponse> getAllUsers();

    AdminUserResponse getUserById(Long userId);

    AdminUserResponse createUser(AdminCreateUserRequest request);

    AdminUserResponse updateUser(Long userId, AdminUpdateUserRequest request);

    void deleteUser(Long userId);

    AdminUserResponse updateUserStatus(Long userId, String status);

    AdminDashboardStats getDashboardStats();
}
