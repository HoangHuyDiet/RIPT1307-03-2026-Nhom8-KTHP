# Database - Smart Finance Hub

## Requirements

- MySQL 8.0+
- Charset: `utf8mb4`

## Setup

### 1. Create database

```sql
CREATE DATABASE smart_finance_hub
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 2. Run schema

```bash
mysql -u root -p smart_finance_hub < data/schema.sql
```

### 3. Run seed data

```bash
mysql -u root -p smart_finance_hub < data/seed.sql
```

## Files

| File | Description |
|---|---|
| `schema.sql` | DDL for 19 current backend tables |
| `seed.sql` | Default roles, permissions, admin user, and categories |
| `seed_admin.sql` | Admin seed helper |

## Tables

| # | Table | Module | Description |
|---|---|---|---|
| 1 | `users` | Auth | Application users |
| 2 | `roles` | Auth | Roles |
| 3 | `user_role` | Auth | User-role mapping |
| 4 | `permissions` | Auth | Permissions |
| 5 | `role_permissions` | Auth | Role-permission mapping |
| 6 | `otp_tokens` | Auth/2FA | OTP verification tokens |
| 7 | `categories` | Core | Income/expense categories |
| 8 | `saving_goals` | Core | Saving goals |
| 9 | `monthly_statements` | Core | Monthly reports |
| 10 | `share_funds` | Funds | Group/shared funds |
| 11 | `fund_members` | Funds | Fund members |
| 12 | `fund_invitation` | Funds | Fund invitations and proposals |
| 13 | `fund_messages` | Funds | Fund chat messages |
| 14 | `fund_activities` | Funds | Fund activity log |
| 15 | `transactions` | Core/Funds | Personal and fund transactions |
| 16 | `recurring_settings` | Scheduler | Recurring transaction settings |
| 17 | `support_tickets` | Support | Support tickets |
| 18 | `audit_logs` | System | Audit logs |
| 19 | `external_integrations` | System | External integration configs |
