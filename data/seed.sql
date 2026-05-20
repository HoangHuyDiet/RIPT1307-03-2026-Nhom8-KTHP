-- ============================================================
-- SMART FINANCE HUB - DỮ LIỆU MẪU (SEED DATA)
-- Chạy sau khi đã chạy schema.sql
-- ============================================================
-- Chạy: mysql -u root -p smart_finance_hub < database/seed.sql
-- ============================================================

USE smart_finance_hub;

-- ============================================================
-- 1. ROLES (Vai trò mặc định)
-- ============================================================
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Quản trị viên hệ thống - Toàn quyền'),
('USER',  'Người dùng thông thường');

-- ============================================================
-- 2. PERMISSIONS (Quyền hạn cơ bản)
-- ============================================================
INSERT INTO permissions (name, description) VALUES
-- Quản lý người dùng
('MANAGE_USERS',            'Quản lý tất cả người dùng'),
('VIEW_ALL_USERS',          'Xem danh sách tất cả người dùng'),

-- Giao dịch
('CREATE_TRANSACTION',      'Tạo giao dịch mới'),
('VIEW_OWN_TRANSACTIONS',   'Xem giao dịch của mình'),
('VIEW_ALL_TRANSACTIONS',   'Xem tất cả giao dịch (Admin)'),
('DELETE_TRANSACTION',      'Xóa giao dịch'),

-- Danh mục
('MANAGE_CATEGORIES',       'Tạo/sửa/xóa danh mục'),

-- Báo cáo
('VIEW_OWN_REPORTS',        'Xem báo cáo của mình'),
('VIEW_ALL_REPORTS',        'Xem tất cả báo cáo (Admin)'),
('EXPORT_REPORTS',          'Xuất báo cáo PDF/Excel'),

-- Quỹ chung
('CREATE_FUND',             'Tạo quỹ chung'),
('MANAGE_FUND_MEMBERS',     'Quản lý thành viên quỹ'),

-- Ticket hỗ trợ
('CREATE_TICKET',           'Tạo ticket hỗ trợ'),
('MANAGE_TICKETS',          'Quản lý tất cả ticket (Admin)'),

-- Hệ thống
('VIEW_AUDIT_LOGS',         'Xem nhật ký hệ thống (Admin)'),
('MANAGE_INTEGRATIONS',     'Quản lý tích hợp bên ngoài');

-- ============================================================
-- 3. ROLE_PERMISSIONS (Gán quyền cho role)
-- ============================================================

-- ADMIN có tất cả quyền
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    id
FROM permissions;

-- USER có các quyền cơ bản
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'USER'),
    id
FROM permissions
WHERE name IN (
    'CREATE_TRANSACTION',
    'VIEW_OWN_TRANSACTIONS',
    'DELETE_TRANSACTION',
    'MANAGE_CATEGORIES',
    'VIEW_OWN_REPORTS',
    'EXPORT_REPORTS',
    'CREATE_FUND',
    'MANAGE_FUND_MEMBERS',
    'CREATE_TICKET',
    'MANAGE_INTEGRATIONS'
);

-- ============================================================
-- 4. ADMIN MẶC ĐỊNH
-- Password: Admin@123 (BCrypt hash)
-- ============================================================
INSERT INTO users (email, password, display_name, status) VALUES
('admin@smartfinance.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'System Admin',
 'ACTIVE');

-- Gán role ADMIN cho user admin
INSERT INTO user_role (user_id, role_id)
VALUES (
    (SELECT id FROM users WHERE email = 'admin@smartfinance.com'),
    (SELECT id FROM roles WHERE name = 'ADMIN')
);

-- ============================================================
-- 5. DANH MỤC MẶC ĐỊNH (Categories)
-- user_id = NULL → danh mục hệ thống dùng chung cho tất cả user
-- ============================================================

-- Danh mục CHI (EXPENSE)
INSERT INTO categories (user_id, name, type, description) VALUES
(NULL, 'Ăn uống',       'EXPENSE', 'Chi phí ăn uống, đồ uống hàng ngày'),
(NULL, 'Di chuyển',     'EXPENSE', 'Xăng, xe bus, grab, taxi...'),
(NULL, 'Mua sắm',      'EXPENSE', 'Quần áo, đồ dùng, mỹ phẩm...'),
(NULL, 'Giải trí',     'EXPENSE', 'Xem phim, du lịch, game...'),
(NULL, 'Giáo dục',     'EXPENSE', 'Học phí, sách vở, khóa học...'),
(NULL, 'Sức khỏe',     'EXPENSE', 'Khám bệnh, thuốc, gym...'),
(NULL, 'Nhà cửa',      'EXPENSE', 'Tiền thuê, điện, nước, internet...'),
(NULL, 'Hóa đơn',      'EXPENSE', 'Điện thoại, bảo hiểm, thuế...'),
(NULL, 'Quà tặng',     'EXPENSE', 'Sinh nhật, lễ tết, từ thiện...'),
(NULL, 'Chi khác',     'EXPENSE', 'Các khoản chi phí khác');

-- Danh mục THU (INCOME)
INSERT INTO categories (user_id, name, type, description) VALUES
(NULL, 'Lương',         'INCOME', 'Lương tháng, lương thưởng'),
(NULL, 'Thưởng',       'INCOME', 'Thưởng KPI, thưởng dự án'),
(NULL, 'Đầu tư',       'INCOME', 'Lãi cổ phiếu, crypto, bất động sản...'),
(NULL, 'Bán hàng',     'INCOME', 'Thu nhập từ kinh doanh, bán đồ'),
(NULL, 'Freelance',     'INCOME', 'Thu nhập làm thêm, freelance'),
(NULL, 'Thu khác',     'INCOME', 'Các khoản thu nhập khác');

-- ============================================================
-- ✅ SEED HOÀN TẤT
-- Admin: admin@smartfinance.com / Admin@123
-- 2 Roles: ADMIN, USER
-- 17 Permissions
-- 16 Danh mục mặc định (10 chi + 6 thu)
-- ============================================================
