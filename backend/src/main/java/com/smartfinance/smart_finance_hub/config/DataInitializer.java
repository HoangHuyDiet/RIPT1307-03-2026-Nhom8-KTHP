package com.smartfinance.smart_finance_hub.config;

import com.smartfinance.smart_finance_hub.entity.Role;
import com.smartfinance.smart_finance_hub.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRoleIfNotExists("ADMIN", "Quản trị viên hệ thống - toàn quyền");
        createRoleIfNotExists("SUPPORT_ADMIN", "Tư vấn viên tài chính - hỗ trợ premium user");
        createRoleIfNotExists("USER", "Người dùng thông thường");
        log.info("=== DataInitializer: Đã khởi tạo roles thành công ===");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = Role.builder()
                .name(name)
                .description(description)
                .build();
            roleRepository.save(role);
            log.info("Đã tạo role: {}", name);
        } else {
            log.info("Role {} đã tồn tại, bỏ qua", name);
        }
    }
}
