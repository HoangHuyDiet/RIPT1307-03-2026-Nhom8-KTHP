-- ============================================================
-- SEED ADMIN & SUPPORT ADMIN ACCOUNTS
-- Chạy SAU KHI backend đã khởi động ít nhất 1 lần
-- (để DataInitializer tạo roles ADMIN, SUPPORT_ADMIN, USER)
-- ============================================================
-- mysql -u root -p smart_finance_hub < data/seed_admin.sql
-- ============================================================

-- ==================== ADMIN ACCOUNT ====================
-- Email: admin@smartfinance.com | Password: Admin@123

INSERT INTO users (email, password, display_name, status, created_at, updated_at)
VALUES (
    'admin@smartfinance.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'System Admin',
    'ACTIVE',
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE display_name = display_name;

-- Gán role ADMIN
INSERT INTO user_role (user_id, role_id, assigned_at)
SELECT u.id, r.id, NOW()
FROM users u, roles r
WHERE u.email = 'admin@smartfinance.com'
  AND r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_role ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- ==================== SUPPORT ADMIN ACCOUNT ====================
-- Email: support@smartfinance.com | Password: Support@123

INSERT INTO users (email, password, display_name, status, created_at, updated_at)
VALUES (
    'support@smartfinance.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Support Admin',
    'ACTIVE',
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE display_name = display_name;

-- Gán role SUPPORT_ADMIN
INSERT INTO user_role (user_id, role_id, assigned_at)
SELECT u.id, r.id, NOW()
FROM users u, roles r
WHERE u.email = 'support@smartfinance.com'
  AND r.name = 'SUPPORT_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_role ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- ============================================================
-- KIỂM TRA:
-- SELECT u.email, u.display_name, r.name as role_name
-- FROM users u
-- JOIN user_role ur ON ur.user_id = u.id
-- JOIN roles r ON r.id = ur.role_id
-- WHERE u.email IN ('admin@smartfinance.com', 'support@smartfinance.com');
-- ============================================================
