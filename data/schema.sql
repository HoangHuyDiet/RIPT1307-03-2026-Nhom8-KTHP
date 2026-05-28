-- ============================================================
-- SMART FINANCE HUB - DATABASE SCHEMA
-- 16 bảng | 6 module | MySQL 8.0+
-- ============================================================
-- Chạy: mysql -u root -p smart_finance_hub < database/schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_finance_hub
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE smart_finance_hub;

-- ============================================================
-- MODULE 1: AUTHENTICATION & AUTHORIZATION (5 bảng)
-- ============================================================

-- 1. Bảng người dùng
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE COMMENT 'Email đăng nhập',
    password        VARCHAR(255)    NOT NULL COMMENT 'Mật khẩu đã hash (BCrypt)',
    display_name    VARCHAR(100)    NOT NULL COMMENT 'Tên hiển thị',
    status          VARCHAR(20)     NOT NULL DEFAULT 'INACTIVE' COMMENT 'ACTIVE / INACTIVE / BANNED',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL COMMENT 'Soft delete (null = chưa xóa)',

    INDEX idx_users_email (email),
    INDEX idx_users_status (status)
) ENGINE=InnoDB COMMENT='Bảng người dùng';

-- 2. Bảng vai trò
CREATE TABLE IF NOT EXISTS roles (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL UNIQUE COMMENT 'Tên role: ADMIN, USER',
    description     VARCHAR(255)    NULL COMMENT 'Mô tả vai trò',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Bảng vai trò';

-- 3. Bảng trung gian: user <-> role (N:M)
CREATE TABLE IF NOT EXISTS user_role (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    assigned_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày gán role',
    expired_at      DATETIME        NULL COMMENT 'Ngày hết hạn (nullable)',

    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_role_user (user_id),
    INDEX idx_user_role_role (role_id)
) ENGINE=InnoDB COMMENT='Bảng trung gian user-role';

-- 4. Bảng quyền hạn
CREATE TABLE IF NOT EXISTS permissions (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL UNIQUE COMMENT 'Tên quyền: CREATE_TRANSACTION, VIEW_REPORT...',
    description     VARCHAR(255)    NULL COMMENT 'Mô tả quyền',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Bảng quyền hạn';

-- 5. Bảng trung gian: role <-> permission (N:M)
CREATE TABLE IF NOT EXISTS role_permissions (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    role_id         BIGINT          NOT NULL,
    permission_id   BIGINT          NOT NULL,
    assigned_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày gán quyền',

    CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_perm_role (role_id),
    INDEX idx_role_perm_perm (permission_id)
) ENGINE=InnoDB COMMENT='Bảng trung gian role-permission';

-- 6. Bảng OTP tokens (dùng cho xác thực email 2FA)
CREATE TABLE IF NOT EXISTS otp_tokens (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    email               VARCHAR(255)    NOT NULL COMMENT 'Email nhận OTP',
    otp_code            VARCHAR(6)      NOT NULL COMMENT 'Mã OTP 6 số',
    expiration_time     DATETIME        NOT NULL COMMENT 'Thời gian hết hạn',
    is_used             BOOLEAN         NOT NULL DEFAULT FALSE COMMENT 'Đã sử dụng chưa',

    INDEX idx_otp_email (email)
) ENGINE=InnoDB COMMENT='Bảng lưu mã OTP xác thực email';

-- ============================================================
-- MODULE 2: CORE - GIAO DỊCH & DANH MỤC (4 bảng)
-- ============================================================


-- 6. Bảng danh mục thu/chi
CREATE TABLE IF NOT EXISTS categories (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NULL COMMENT 'Người tạo (NULL = danh mục hệ thống dùng chung)',
    name            VARCHAR(100)    NOT NULL COMMENT 'Tên: Ăn uống, Lương...',
    type            VARCHAR(20)     NOT NULL COMMENT 'INCOME / EXPENSE',
    description     VARCHAR(255)    NULL COMMENT 'Mô tả danh mục',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL COMMENT 'Soft delete',

    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_categories_user (user_id),
    INDEX idx_categories_type (type)
) ENGINE=InnoDB COMMENT='Bảng danh mục thu/chi';

-- 7. Bảng quỹ nhóm
CREATE TABLE IF NOT EXISTS funds (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL COMMENT 'Fund name',
    description     VARCHAR(500)    NULL COMMENT 'Fund description',
    balance         DECIMAL(19,4)   NOT NULL DEFAULT 0.0000 COMMENT 'Current balance',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISBANDED',
    target_amount   DECIMAL(19,4)   NULL COMMENT 'Target amount',
    due_date        DATE            NULL COMMENT 'Due date',
    theme_color     VARCHAR(20)     NULL COMMENT 'Frontend theme color',
    version         BIGINT          NULL DEFAULT 0 COMMENT 'Optimistic locking version',
    created_by      BIGINT          NOT NULL COMMENT 'Creator user id',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_funds_creator FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_funds_created_by (created_by),
    INDEX idx_funds_status (status)
) ENGINE=InnoDB COMMENT='Group funds';

-- 8. Bảng giao dịch
CREATE TABLE IF NOT EXISTS transactions (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL COMMENT 'Người thực hiện',
    category_id     BIGINT          NOT NULL COMMENT 'Danh mục',
    fund_id         BIGINT          NULL COMMENT 'Quỹ chung (nullable - giao dịch cá nhân thì null)',
    amount          DECIMAL(19,4)   NOT NULL COMMENT 'Số tiền (chuẩn tài chính 19,4)',
    type            VARCHAR(20)     NOT NULL COMMENT 'INCOME / EXPENSE',
    description     VARCHAR(500)    NULL COMMENT 'Mô tả giao dịch',
    date            DATE            NOT NULL COMMENT 'Ngày giao dịch',
    is_approved     BOOLEAN         NOT NULL DEFAULT FALSE COMMENT 'TRUE neu giao dich da duoc phe duyet',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / APPROVED / REJECTED',
    bank_account    VARCHAR(100)    NULL COMMENT 'Bank account for withdrawal request',
    bank_name       VARCHAR(100)    NULL COMMENT 'Bank name for withdrawal request',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_transactions_fund FOREIGN KEY (fund_id) REFERENCES funds(id) ON DELETE SET NULL,
    INDEX idx_transactions_user (user_id),
    INDEX idx_transactions_category (category_id),
    INDEX idx_transactions_fund (fund_id),
    INDEX idx_transactions_date (date),
    INDEX idx_transactions_fund_status (fund_id, status),
    INDEX idx_transactions_fund_approved_date (fund_id, is_approved, date),
    INDEX idx_transactions_user_fund (user_id, fund_id),
    INDEX idx_transactions_fund_type (fund_id, type)
) ENGINE=InnoDB COMMENT='Bảng giao dịch';

-- 9. Bảng mục tiêu tiết kiệm
CREATE TABLE IF NOT EXISTS saving_goals (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL COMMENT 'Tên: Mua xe, Du lịch...',
    target_amount   DECIMAL(19,4)   NOT NULL COMMENT 'Số tiền mục tiêu (chuẩn tài chính 19,4)',
    current_amount  DECIMAL(19,4)   NOT NULL DEFAULT 0.0000 COMMENT 'Số tiền hiện có',
    due_date        DATE            NULL COMMENT 'Ngày đến hạn',
    status          VARCHAR(20)     NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS / COMPLETED / FAILED',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_saving_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_saving_goals_user (user_id),
    INDEX idx_saving_goals_status (status)
) ENGINE=InnoDB COMMENT='Bảng mục tiêu tiết kiệm';

-- 10. Bảng báo cáo tài chính hàng tháng
CREATE TABLE IF NOT EXISTS monthly_statements (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    month           VARCHAR(7)      NOT NULL COMMENT 'Tháng: 2026-05',
    pdf_url         VARCHAR(500)    NULL COMMENT 'URL file PDF',
    ai_summary      TEXT            NULL COMMENT 'Tóm tắt bởi AI',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_monthly_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_month (user_id, month),
    INDEX idx_monthly_user (user_id)
) ENGINE=InnoDB COMMENT='Báo cáo tài chính hàng tháng';

-- ============================================================
-- MODULE 3: GROUP FUND MEMBERSHIP (3 bảng)
-- ============================================================

-- 11. Bảng thành viên quỹ (user <-> funds, N:M)
CREATE TABLE IF NOT EXISTS fund_members (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    fund_id         BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    fund_role       VARCHAR(20)     NOT NULL DEFAULT 'MEMBER' COMMENT 'OWNER / MEMBER',
    joined_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_fund_members_fund FOREIGN KEY (fund_id) REFERENCES funds(id) ON DELETE CASCADE,
    CONSTRAINT fk_fund_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_fund_member (fund_id, user_id),
    INDEX idx_fund_members_fund (fund_id),
    INDEX idx_fund_members_user (user_id),
    INDEX idx_fund_members_role (fund_id, fund_role)
) ENGINE=InnoDB COMMENT='Bảng thành viên quỹ chung';

-- 12. Bảng lời mời tham gia quỹ
CREATE TABLE IF NOT EXISTS fund_invitation (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    fund_id             BIGINT          NOT NULL,
    invited_email       VARCHAR(255)    NOT NULL COMMENT 'Email người được mời',
    invitation_token    VARCHAR(255)    NOT NULL UNIQUE COMMENT 'Token xác thực',
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / ACCEPTED / REJECTED / EXPIRED / CANCELLED',
    type                VARCHAR(50)     NULL COMMENT 'MEMBER_INVITE / KICK_PROPOSAL / DISBAND_PROPOSAL',
    reason              VARCHAR(500)    NULL COMMENT 'Reason for kick/disband proposal',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at          DATETIME        NOT NULL COMMENT 'Ngày hết hạn',

    CONSTRAINT fk_fund_invitation_fund FOREIGN KEY (fund_id) REFERENCES funds(id) ON DELETE CASCADE,
    INDEX idx_fund_invitation_fund (fund_id),
    INDEX idx_fund_invitation_token (invitation_token),
    INDEX idx_fund_invitation_email (invited_email),
    INDEX idx_fund_invitation_fund_type_status (fund_id, type, status),
    INDEX idx_fund_invitation_email_status (invited_email, status)
) ENGINE=InnoDB COMMENT='Bảng lời mời tham gia quỹ';

CREATE TABLE IF NOT EXISTS fund_messages (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    fund_id         BIGINT          NOT NULL,
    sender_id       BIGINT          NULL,
    type            VARCHAR(20)     NOT NULL DEFAULT 'user' COMMENT 'user / system',
    text            VARCHAR(1000)   NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_fund_messages_fund FOREIGN KEY (fund_id) REFERENCES funds(id) ON DELETE CASCADE,
    CONSTRAINT fk_fund_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_fund_messages_fund_created_at (fund_id, created_at)
) ENGINE=InnoDB COMMENT='Fund chat messages';

-- ============================================================
-- MODULE 4: HỖ TRỢ - SUPPORT (1 bảng)
-- ============================================================

-- 13. Bảng ticket hỗ trợ
CREATE TABLE IF NOT EXISTS support_tickets (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT          NOT NULL COMMENT 'Người gửi ticket',
    assigned_admin_id   BIGINT          NULL COMMENT 'Admin xử lý (nullable)',
    subject             VARCHAR(255)    NOT NULL COMMENT 'Tiêu đề',
    description         TEXT            NOT NULL COMMENT 'Nội dung chi tiết',
    priority            VARCHAR(20)     NOT NULL DEFAULT 'MEDIUM' COMMENT 'LOW / MEDIUM / HIGH / URGENT',
    status              VARCHAR(20)     NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN / IN_PROGRESS / RESOLVED / CLOSED',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tickets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tickets_admin FOREIGN KEY (assigned_admin_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_tickets_user (user_id),
    INDEX idx_tickets_admin (assigned_admin_id),
    INDEX idx_tickets_status (status),
    INDEX idx_tickets_priority (priority)
) ENGINE=InnoDB COMMENT='Bảng ticket hỗ trợ';

-- ============================================================
-- MODULE 5: HỆ THỐNG - SYSTEM (2 bảng)
-- ============================================================

-- 14. Bảng nhật ký thao tác
CREATE TABLE IF NOT EXISTS audit_logs (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    action          VARCHAR(50)     NOT NULL COMMENT 'CREATE / UPDATE / DELETE / LOGIN',
    entity_type     VARCHAR(100)    NOT NULL COMMENT 'Transaction, User, Category...',
    details         TEXT            NULL COMMENT 'Chi tiết thay đổi (JSON)',
    ip_address      VARCHAR(45)     NULL COMMENT 'Địa chỉ IP (hỗ trợ IPv6)',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm xảy ra',

    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity (entity_type),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB COMMENT='Nhật ký thao tác hệ thống';

-- 15. Bảng tích hợp bên ngoài
CREATE TABLE IF NOT EXISTS external_integrations (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT          NULL COMMENT 'Người dùng (NULL = cấu hình hệ thống dùng chung)',
    provider_name       VARCHAR(100)    NOT NULL COMMENT 'Google, Bank...',
    integration_type    VARCHAR(50)     NOT NULL COMMENT 'OAUTH / API_KEY / WEBHOOK',
    config_json         JSON            NULL COMMENT 'Cấu hình JSON (MySQL tự validate cú pháp)',
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_integrations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_integrations_user (user_id),
    INDEX idx_integrations_provider (provider_name)
) ENGINE=InnoDB COMMENT='Tích hợp dịch vụ bên ngoài';

-- ============================================================
-- MODULE 6: XÁC THỰC 2 LỚP - 2FA (1 bảng)
-- ============================================================

-- 16. Bảng lưu trữ mã OTP tạm thời
CREATE TABLE IF NOT EXISTS otp_tokens (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL COMMENT 'Email nhận mã',
    otp_code        VARCHAR(6)      NOT NULL COMMENT 'Mã OTP 6 số',
    expiration_time DATETIME        NOT NULL COMMENT 'Thời điểm hết hạn',
    is_used         BOOLEAN         NOT NULL DEFAULT FALSE COMMENT 'Trạng thái sử dụng',

    INDEX idx_otp_email_code (email, otp_code)
) ENGINE=InnoDB COMMENT='Bảng lưu trữ mã OTP tạm thời';

-- ============================================================
-- MODULE 7: CAU HINH DINH KY (1 bang)
-- ============================================================

CREATE TABLE IF NOT EXISTS recurring_settings (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    category_id     BIGINT          NOT NULL,
    amount          DECIMAL(19,4)   NOT NULL,
    type            VARCHAR(20)     NOT NULL COMMENT 'INCOME / EXPENSE',
    description     VARCHAR(500)    NULL,
    frequency       VARCHAR(20)     NOT NULL DEFAULT 'MONTHLY' COMMENT 'DAILY / WEEKLY / MONTHLY',
    day_of_month    INT             NULL COMMENT '1-31',
    day_of_week     INT             NULL COMMENT '1=MON, 7=SUN',
    start_date      DATE            NOT NULL,
    end_date        DATE            NULL COMMENT 'NULL = vo thoi han',
    next_run_date   DATE            NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    last_run_at     DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_recurring_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_recurring_user (user_id),
    INDEX idx_recurring_next_run (next_run_date),
    INDEX idx_recurring_active (is_active)
) ENGINE=InnoDB COMMENT='Cau hinh thu chi dinh ky';
