package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.AdminCreateUserRequest;
import com.smartfinance.smart_finance_hub.dto.request.AdminUpdateUserRequest;
import com.smartfinance.smart_finance_hub.dto.response.AdminDashboardStats;
import com.smartfinance.smart_finance_hub.dto.response.AdminUserResponse;
import com.smartfinance.smart_finance_hub.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    /**
     * Lấy danh sách tất cả user (ADMIN + SUPPORT_ADMIN)
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        log.info("Admin API: GET /api/admin/users");
        List<AdminUserResponse> users = adminService.getAllUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Lấy danh sách user thành công");
        response.put("data", users);

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy chi tiết user (ADMIN + SUPPORT_ADMIN)
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        log.info("Admin API: GET /api/admin/users/{}", id);
        AdminUserResponse user = adminService.getUserById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Lấy thông tin user thành công");
        response.put("data", user);

        return ResponseEntity.ok(response);
    }

    /**
     * Tạo user mới (Chỉ ADMIN)
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        log.info("Admin API: POST /api/admin/users");
        AdminUserResponse user = adminService.createUser(request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "Tạo user thành công");
        response.put("data", user);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin user (Chỉ ADMIN)
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        log.info("Admin API: PUT /api/admin/users/{}", id);
        AdminUserResponse user = adminService.updateUser(id, request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Cập nhật user thành công");
        response.put("data", user);

        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật trạng thái user (ACTIVE/BANNED) - Chỉ ADMIN
     */
    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        log.info("Admin API: PUT /api/admin/users/{}/status", id);

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Trường 'status' là bắt buộc");
        }

        AdminUserResponse user = adminService.updateUserStatus(id, status);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Cập nhật trạng thái user thành công");
        response.put("data", user);

        return ResponseEntity.ok(response);
    }

    /**
     * Xóa user (Chỉ ADMIN)
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        log.info("Admin API: DELETE /api/admin/users/{}", id);
        adminService.deleteUser(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Xóa user thành công");

        return ResponseEntity.ok(response);
    }

    /**
     * Thống kê tổng quan dashboard (ADMIN + SUPPORT_ADMIN)
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Admin API: GET /api/admin/dashboard/stats");
        AdminDashboardStats stats = adminService.getDashboardStats();

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Lấy thống kê thành công");
        response.put("data", stats);

        return ResponseEntity.ok(response);
    }
}
