# 🗄️ Database - Smart Finance Hub

## Yêu cầu
- **MySQL** 8.0+
- Charset: `utf8mb4`

## Hướng dẫn setup

### 1. Tạo database
```sql
CREATE DATABASE smart_finance_hub
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 2. Chạy schema (tạo bảng)
```bash
mysql -u root -p smart_finance_hub < database/schema.sql
```

### 3. Chạy seed (dữ liệu mẫu)
```bash
mysql -u root -p smart_finance_hub < database/seed.sql
```

### 4. Cấu hình backend
Sửa file `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_finance_hub
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

## Cấu trúc file
| File | Mô tả |
|---|---|
| `schema.sql` | DDL - Tạo 16 bảng (15 bảng core + 1 bảng otp_tokens cho 2FA) |
| `seed.sql` | DML - Dữ liệu mặc định (roles, permissions, categories) |

## Danh sách bảng (16 bảng)
| # | Bảng | Module | Mô tả |
|---|------|--------|-------|
| 1 | `users` | Auth | Người dùng |
| 2 | `roles` | Auth | Vai trò |
| 3 | `user_role` | Auth | Trung gian User ↔ Role |
| 4 | `permissions` | Auth | Quyền hạn |
| 5 | `role_permissions` | Auth | Trung gian Role ↔ Permission |
| 6 | `categories` | Core | Danh mục thu/chi |
| 7 | `transactions` | Core | Giao dịch |
| 8 | `saving_goals` | Core | Mục tiêu tiết kiệm |
| 9 | `monthly_statements` | Core | Báo cáo tháng |
| 10 | `share_funds` | Funds | Quỹ chung |
| 11 | `fund_members` | Funds | Thành viên quỹ |
| 12 | `fund_invitation` | Funds | Lời mời tham gia quỹ |
| 13 | `support_tickets` | Support | Ticket hỗ trợ |
| 14 | `audit_logs` | System | Nhật ký thao tác |
| 15 | `external_integrations` | System | Tích hợp bên ngoài |
| 16 | `otp_tokens` | 2FA | Lưu trữ mã OTP tạm thời |
