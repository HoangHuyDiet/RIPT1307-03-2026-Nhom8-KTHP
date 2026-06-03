package com.smartfinance.smart_finance_hub.config;

import com.smartfinance.smart_finance_hub.entity.Category;
import com.smartfinance.smart_finance_hub.entity.Role;
import com.smartfinance.smart_finance_hub.repository.CategoryRepository;
import com.smartfinance.smart_finance_hub.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        createRoleIfNotExists("ADMIN", "Quản trị viên hệ thống - toàn quyền");
        createRoleIfNotExists("SUPPORT_ADMIN", "Tư vấn viên tài chính - hỗ trợ premium user");
        createRoleIfNotExists("USER", "Người dùng thông thường");
        allowSystemCategoriesWithoutUser();
        createDefaultSystemCategories();
        log.info("=== DataInitializer: Đã khởi tạo dữ liệu mặc định thành công ===");
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
            log.warn("Không thể cập nhật nullable cho categories.user_id, bỏ qua nếu schema đã đúng: {}", error.getMessage());
        }
    }
}
