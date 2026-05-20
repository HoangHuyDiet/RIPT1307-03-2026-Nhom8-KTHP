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
| `schema.sql` | DDL - Tạo 15 bảng từ ERD |
| `seed.sql` | DML - Dữ liệu mặc định (roles, permissions, categories) |
