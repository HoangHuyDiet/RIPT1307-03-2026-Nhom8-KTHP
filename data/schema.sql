-- ============================================================
-- SMART FINANCE HUB - DATABASE SCHEMA
-- 20 tables | MySQL 8.0+
-- ============================================================
-- Run:
--   mysql -u root -p < data/schema.sql
-- or:
--   mysql -u root -p smart_finance_hub < data/schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_finance_hub
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE smart_finance_hub;

-- ============================================================
-- MODULE 1: AUTHENTICATION & AUTHORIZATION
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE COMMENT 'Login email',
    password        VARCHAR(255)    NOT NULL COMMENT 'BCrypt password hash',
    display_name    VARCHAR(100)    NOT NULL COMMENT 'Display name',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / INACTIVE / BANNED',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL COMMENT 'Soft delete timestamp',

    INDEX idx_users_email (email),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Application users';

CREATE TABLE IF NOT EXISTS roles (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL UNIQUE COMMENT 'ADMIN, SUPPORT_ADMIN, USER',
    description     VARCHAR(255)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Roles';

CREATE TABLE IF NOT EXISTS permissions (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL UNIQUE,
    description     VARCHAR(255)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Permissions';

CREATE TABLE IF NOT EXISTS user_role (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    assigned_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at      DATETIME        NULL,

    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_role_user (user_id),
    INDEX idx_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-role mapping';

CREATE TABLE IF NOT EXISTS role_permissions (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    role_id         BIGINT          NOT NULL,
    permission_id   BIGINT          NOT NULL,
    assigned_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_perm_role (role_id),
    INDEX idx_role_perm_perm (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role-permission mapping';

CREATE TABLE IF NOT EXISTS otp_tokens (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL,
    otp_code        VARCHAR(6)      NOT NULL,
    expiration_time DATETIME        NOT NULL,
    is_used         BOOLEAN         NOT NULL DEFAULT FALSE,

    INDEX idx_otp_email_code (email, otp_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OTP verification tokens';

-- ============================================================
-- MODULE 2: CORE FINANCE
-- ============================================================

CREATE TABLE IF NOT EXISTS categories (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NULL,
    name            VARCHAR(100)    NOT NULL,
    type            VARCHAR(20)     NOT NULL COMMENT 'INCOME / EXPENSE',
    description     VARCHAR(255)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL,

    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_categories_user (user_id),
    INDEX idx_categories_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Income and expense categories';

CREATE TABLE IF NOT EXISTS saving_goals (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    category_id     BIGINT          NULL,
    name            VARCHAR(100)    NOT NULL,
    target_amount   DECIMAL(19,4)   NOT NULL,
    current_amount  DECIMAL(19,4)   NOT NULL DEFAULT 0.0000,
    due_date        DATE            NULL,
    is_pinned       BOOLEAN         NOT NULL DEFAULT FALSE,
    currency        VARCHAR(3)      NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS / COMPLETED / CANCELLED',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME        NULL,

    CONSTRAINT fk_saving_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_saving_goals_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_saving_goals_user (user_id),
    INDEX idx_saving_goals_category (category_id),
    INDEX idx_saving_goals_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Saving goals';

CREATE TABLE IF NOT EXISTS monthly_statements (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    month           VARCHAR(7)      NOT NULL COMMENT 'YYYY-MM',
    pdf_url         VARCHAR(500)    NULL,
    ai_summary      TEXT            NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_monthly_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_month (user_id, month),
    INDEX idx_monthly_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Monthly reports';

-- ============================================================
-- MODULE 3: SHARE FUNDS
-- ============================================================

CREATE TABLE IF NOT EXISTS share_funds (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(500)    NULL,
    balance         DECIMAL(19,4)   NOT NULL DEFAULT 0.0000,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / CLOSED',
    created_by      BIGINT          NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_share_funds_creator FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_share_funds_creator (created_by),
    INDEX idx_share_funds_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shared funds';

CREATE TABLE IF NOT EXISTS fund_members (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    fund_id         BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    fund_role       VARCHAR(20)     NOT NULL DEFAULT 'MEMBER' COMMENT 'OWNER / MEMBER',
    joined_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_fund_members_fund FOREIGN KEY (fund_id) REFERENCES share_funds(id) ON DELETE CASCADE,
    CONSTRAINT fk_fund_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_fund_member (fund_id, user_id),
    INDEX idx_fund_members_fund (fund_id),
    INDEX idx_fund_members_user (user_id),
    INDEX idx_fund_members_role (fund_id, fund_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shared fund members';

CREATE TABLE IF NOT EXISTS fund_invitation (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    fund_id             BIGINT          NOT NULL,
    invited_email       VARCHAR(255)    NOT NULL,
    invitation_token    VARCHAR(255)    NOT NULL UNIQUE,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / ACCEPTED / REJECTED',
    type                VARCHAR(50)     NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at          DATETIME        NOT NULL,

    CONSTRAINT fk_fund_invitation_fund FOREIGN KEY (fund_id) REFERENCES share_funds(id) ON DELETE CASCADE,
    INDEX idx_fund_invitation_fund (fund_id),
    INDEX idx_fund_invitation_token (invitation_token),
    INDEX idx_fund_invitation_email (invited_email),
    INDEX idx_fund_invitation_fund_type_status (fund_id, type, status),
    INDEX idx_fund_invitation_email_status (invited_email, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shared fund invitations and proposals';

CREATE TABLE IF NOT EXISTS fund_messages (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    fund_id         BIGINT          NOT NULL,
    sender_id       BIGINT          NULL,
    type            VARCHAR(20)     NOT NULL DEFAULT 'user',
    text            VARCHAR(1000)   NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_fund_messages_fund FOREIGN KEY (fund_id) REFERENCES share_funds(id) ON DELETE CASCADE,
    CONSTRAINT fk_fund_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_fund_messages_fund_created_at (fund_id, created_at),
    INDEX idx_fund_messages_sender (sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shared fund chat messages';

CREATE TABLE IF NOT EXISTS fund_activities (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NULL,
    fund_id         BIGINT          NOT NULL,
    type            VARCHAR(50)     NOT NULL,
    text            VARCHAR(500)    NOT NULL,
    color           VARCHAR(20)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_fund_activities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_fund_activities_fund FOREIGN KEY (fund_id) REFERENCES share_funds(id) ON DELETE CASCADE,
    INDEX idx_fund_activities_user_created (user_id, created_at),
    INDEX idx_fund_activities_fund_created (fund_id, created_at),
    INDEX idx_fund_activities_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shared fund activity log';

CREATE TABLE IF NOT EXISTS personal_funds (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    fund_type       VARCHAR(20)     NOT NULL COMMENT 'CASH / BANK_ACCOUNT / CREDIT_CARD / E_WALLET / INVESTMENT',
    balance         DECIMAL(19,4)   NOT NULL DEFAULT 0.0000,
    currency        VARCHAR(3)      NULL DEFAULT 'VND',
    description     VARCHAR(500)    NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISBANDED / CLOSED',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_personal_funds_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_personal_funds_user (user_id),
    INDEX idx_personal_funds_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Personal funds';

CREATE TABLE IF NOT EXISTS transactions (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    category_id     BIGINT          NULL,
    fund_id         BIGINT          NULL,
    saving_goal_id  BIGINT          NULL,
    personal_fund_id BIGINT         NULL,
    linked_transaction_id BIGINT    NULL,
    amount          DECIMAL(19,4)   NOT NULL,
    type            VARCHAR(20)     NOT NULL COMMENT 'INCOME / EXPENSE / TRANSFER',
    description     VARCHAR(500)    NULL,
    is_approved     BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    bank_account    VARCHAR(100)    NULL,
    bank_name       VARCHAR(100)    NULL,
    approved_by     BIGINT          NULL,
    approved_at     DATETIME        NULL,
    reject_reason   VARCHAR(500)    NULL,
    date            DATE            NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_fund FOREIGN KEY (fund_id) REFERENCES share_funds(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_saving_goal FOREIGN KEY (saving_goal_id) REFERENCES saving_goals(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_personal_fund FOREIGN KEY (personal_fund_id) REFERENCES personal_funds(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_transactions_user (user_id),
    INDEX idx_transactions_category (category_id),
    INDEX idx_transactions_fund (fund_id),
    INDEX idx_transactions_saving_goal (saving_goal_id),
    INDEX idx_transactions_personal_fund (personal_fund_id),
    INDEX idx_transactions_date (date),
    INDEX idx_transactions_fund_status (fund_id, status),
    INDEX idx_transactions_fund_approved_date (fund_id, is_approved, date),
    INDEX idx_transactions_user_fund (user_id, fund_id),
    INDEX idx_transactions_fund_type (fund_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Transactions';

-- ============================================================
-- MODULE 4: SCHEDULER
-- ============================================================

CREATE TABLE IF NOT EXISTS recurring_settings (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    category_id     BIGINT          NOT NULL,
    amount          DECIMAL(19,4)   NOT NULL,
    type            VARCHAR(20)     NOT NULL COMMENT 'INCOME / EXPENSE',
    description     VARCHAR(500)    NULL,
    frequency       VARCHAR(20)     NOT NULL DEFAULT 'MONTHLY' COMMENT 'DAILY / WEEKLY / MONTHLY',
    day_of_month    INT             NULL,
    day_of_week     INT             NULL,
    start_date      DATE            NOT NULL,
    end_date        DATE            NULL,
    next_run_date   DATE            NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    last_run_at     DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_recurring_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_recurring_user (user_id),
    INDEX idx_recurring_category (category_id),
    INDEX idx_recurring_next_run (next_run_date),
    INDEX idx_recurring_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Recurring transaction settings';

-- ============================================================
-- MODULE 5: SUPPORT & SYSTEM
-- ============================================================

CREATE TABLE IF NOT EXISTS support_tickets (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT          NOT NULL,
    assigned_admin_id   BIGINT          NULL,
    subject             VARCHAR(255)    NOT NULL,
    description         TEXT            NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Support tickets';

CREATE TABLE IF NOT EXISTS audit_logs (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    action          VARCHAR(50)     NOT NULL,
    entity_type     VARCHAR(100)    NOT NULL,
    details         TEXT            NULL,
    ip_address      VARCHAR(45)     NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity (entity_type),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Audit logs';

CREATE TABLE IF NOT EXISTS external_integrations (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT          NULL,
    provider_name       VARCHAR(100)    NOT NULL,
    integration_type    VARCHAR(50)     NOT NULL,
    config_json         JSON            NULL,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_integrations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_integrations_user (user_id),
    INDEX idx_integrations_provider (provider_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='External integrations';
