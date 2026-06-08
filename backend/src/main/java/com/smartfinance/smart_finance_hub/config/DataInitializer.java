package com.smartfinance.smart_finance_hub.config;

import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.entity.Permission;
import com.smartfinance.smart_finance_hub.entity.Role;
import com.smartfinance.smart_finance_hub.entity.RolePermission;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.repository.PermissionRepository;
import com.smartfinance.smart_finance_hub.repository.RolePermissionRepository;
import com.smartfinance.smart_finance_hub.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final com.smartfinance.smart_finance_hub.repository.UserRepository userRepository;
    private final com.smartfinance.smart_finance_hub.repository.UserRoleRepository userRoleRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        createRoleIfNotExists("ADMIN", "Quản trị viên hệ thống - toàn quyền");
        createRoleIfNotExists("SUPPORT_ADMIN", "Tư vấn viên tài chính - hỗ trợ premium user");
        createRoleIfNotExists("USER", "Người dùng thông thường");
        allowSystemCategoriesWithoutUser();
        createDefaultSystemCategories();

        initializeAiPermissions();
        
        createDefaultUserIfNotExists("admin@smartfinance.com", "admin123", "ADMIN", "Administrator");
        createDefaultUserIfNotExists("support@smartfinance.com", "support123", "SUPPORT_ADMIN", "Support Admin");

        log.info("=== DataInitializer: Đã khởi tạo dữ liệu mặc định thành công ===");
    }

    private void createDefaultUserIfNotExists(String email, String rawPassword, String roleName, String displayName) {
        if (!userRepository.existsByEmail(email)) {
            com.smartfinance.smart_finance_hub.entity.User user = com.smartfinance.smart_finance_hub.entity.User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .displayName(displayName)
                .status(com.smartfinance.smart_finance_hub.entity.enums.UserStatus.ACTIVE)
                .build();
            userRepository.save(user);

            Role role = roleRepository.findByName(roleName).orElse(null);
            if (role != null) {
                userRoleRepository.save(com.smartfinance.smart_finance_hub.entity.UserRole.builder()
                    .user(user)
                    .role(role)
                    .build());
            }
            log.info("Đã tạo tài khoản mặc định: {} với quyền {}", email, roleName);
        } else {
            log.info("Tài khoản {} đã tồn tại, bỏ qua", email);
        }
    }

    private void initializeAiPermissions() {
        createPermissionIfNotExists("CONSULTATION_VIEW_QUEUE", "Xem hàng chờ tư vấn");
        createPermissionIfNotExists("CONSULTATION_ASSIGN_SELF", "Nhận yêu cầu tư vấn");
        createPermissionIfNotExists("CONSULTATION_VIEW_ASSIGNED", "Xem yêu cầu đã nhận");
        createPermissionIfNotExists("CONSULTATION_REPLY", "Phản hồi yêu cầu tư vấn");
        createPermissionIfNotExists("CONSULTATION_COMPLETE", "Hoàn thành yêu cầu tư vấn");
        createPermissionIfNotExists("RAG_REBUILD", "Kích hoạt rebuild Vector Store thủ công");
        createPermissionIfNotExists("RAG_STATUS_VIEW", "Xem trạng thái RAG Vector Store");

        List<String> supportPermissions = List.of(
            "CONSULTATION_VIEW_QUEUE", "CONSULTATION_ASSIGN_SELF",
            "CONSULTATION_VIEW_ASSIGNED", "CONSULTATION_REPLY",
            "CONSULTATION_COMPLETE", "RAG_REBUILD", "RAG_STATUS_VIEW",
            "SUPPORT_USER_VIEW", "SUPPORT_USER_TOGGLE_STATUS", "SUPPORT_LOCK_REQUEST_CREATE",
            "SUPPORT_LOCK_REQUEST_VIEW", "SUPPORT_LOCK_REQUEST_DELETE", "SUPPORT_AUDIT_LOG_VIEW",
            "SUPPORT_CHAT_VIEW", "SUPPORT_CHAT_SEND", "SUPPORT_CHAT_RESOLVE",
            "SUPPORT_BROADCAST_VIEW", "SUPPORT_BROADCAST_CREATE"
        );
        assignPermissionsToRole("SUPPORT_ADMIN", supportPermissions);

        createPermissionIfNotExists("LOCK_REQUEST_APPROVE", "Phê duyệt yêu cầu khóa tài khoản");
        createPermissionIfNotExists("LOCK_REQUEST_REJECT", "Từ chối yêu cầu khóa tài khoản");
        assignPermissionsToRole("ADMIN", List.of("LOCK_REQUEST_APPROVE", "LOCK_REQUEST_REJECT"));
    }

    private void createPermissionIfNotExists(String name, String description) {
        if (!permissionRepository.existsByName(name)) {
            permissionRepository.save(Permission.builder()
                .name(name)
                .description(description)
                .build());
            log.info("Đã tạo permission: {}", name);
        }
    }

    private void assignPermissionsToRole(String roleName, List<String> permissionNames) {
        Role role = roleRepository.findByName(roleName).orElse(null);
        if (role == null) {
            log.warn("Role {} không tồn tại, bỏ qua gán permissions", roleName);
            return;
        }

        for (String permName : permissionNames) {
            if (!rolePermissionRepository.existsByRoleIdAndPermissionId(
                    role.getId(),
                    permissionRepository.findByName(permName).map(Permission::getId).orElse(-1L))) {
                Permission perm = permissionRepository.findByName(permName).orElse(null);
                if (perm != null) {
                    rolePermissionRepository.save(RolePermission.builder()
                        .role(role)
                        .permission(perm)
                        .build());
                    log.info("Đã gán permission {} cho role {}", permName, roleName);
                }
            }
        }
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

    private void createDefaultSystemCategories() {
        createSystemCategoryIfNotExists("Ăn uống", "EXPENSE", "Chi phí ăn uống, cà phê, nhà hàng");
        createSystemCategoryIfNotExists("Di chuyển", "EXPENSE", "Xăng xe, taxi, vé xe, vé máy bay");
        createSystemCategoryIfNotExists("Đi chơi", "EXPENSE", "Giải trí, xem phim, gặp gỡ bạn bè");
        createSystemCategoryIfNotExists("Du lịch", "EXPENSE", "Chi phí chuyến đi, khách sạn, tham quan");
        createSystemCategoryIfNotExists("Mua sắm", "EXPENSE", "Quần áo, đồ dùng cá nhân, vật dụng gia đình");
        createSystemCategoryIfNotExists("Mua nhà", "EXPENSE", "Đặt cọc, trả góp, sửa chữa hoặc mua nhà");
        createSystemCategoryIfNotExists("Hóa đơn", "EXPENSE", "Điện, nước, internet, điện thoại");
        createSystemCategoryIfNotExists("Sức khỏe", "EXPENSE", "Khám bệnh, thuốc, bảo hiểm y tế");
        createSystemCategoryIfNotExists("Giáo dục", "EXPENSE", "Học phí, sách vở, khóa học");

        createSystemCategoryIfNotExists("Lương", "INCOME", "Thu nhập lương chính");
        createSystemCategoryIfNotExists("Thưởng", "INCOME", "Thưởng, hoa hồng, phụ cấp");
        createSystemCategoryIfNotExists("Kinh doanh", "INCOME", "Doanh thu kinh doanh hoặc bán hàng");
        createSystemCategoryIfNotExists("Đầu tư", "INCOME", "Lãi đầu tư, cổ tức, lợi nhuận tài sản");
        createSystemCategoryIfNotExists("Bán tài sản", "INCOME", "Tiền thu từ bán đồ dùng hoặc tài sản");
        createSystemCategoryIfNotExists("Khác", "INCOME", "Nguồn thu khác");
    }

    private void createSystemCategoryIfNotExists(String name, String type, String description) {
        if (!categoryRepository.existsActiveSystemCategoryName(name, type)) {
            Category category = Category.builder()
                    .name(name)
                    .type(type)
                    .description(description)
                    .build();
            categoryRepository.save(category);
            log.info("Đã tạo danh mục hệ thống: {} ({})", name, type);
        } else {
            log.info("Danh mục hệ thống {} ({}) đã tồn tại, bỏ qua", name, type);
        }
    }

    private void allowSystemCategoriesWithoutUser() {
        try {
            jdbcTemplate.execute("ALTER TABLE categories MODIFY user_id BIGINT NULL");
            log.info("Đã đảm bảo categories.user_id cho phép NULL để lưu danh mục hệ thống");
        } catch (Exception error) {
            log.warn("Không thể cập nhật nullable cho categories.user_id, bỏ qua nếu schema đã đúng: {}",
                    error.getMessage());
        }
    }
}
