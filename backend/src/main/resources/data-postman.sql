INSERT INTO users (id, email, password, display_name, status, created_at, updated_at)
VALUES (
    1,
    'admin@smartfinance.com',
    '$2a$10$A.GKWLaqYJDdRGsryVogw.X5XPIDrEX4.D2DauBcC8VcyhFUh.y5e',
    'System Admin',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO categories (id, user_id, name, type, description, created_at, updated_at)
VALUES
    (1, NULL, 'An uong', 'EXPENSE', 'Chi phi an uong hang ngay', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, NULL, 'Di chuyen', 'EXPENSE', 'Xang xe, xe bus, taxi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, NULL, 'Mua sam', 'EXPENSE', 'Quan ao, do dung', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, NULL, 'Giai tri', 'EXPENSE', 'Xem phim, du lich, game', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, NULL, 'Giao duc', 'EXPENSE', 'Hoc phi, sach vo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, NULL, 'Suc khoe', 'EXPENSE', 'Kham benh, thuoc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, NULL, 'Nha cua', 'EXPENSE', 'Tien thue, dien nuoc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, NULL, 'Hoa don', 'EXPENSE', 'Dien thoai, bao hiem, thue', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, NULL, 'Qua tang', 'EXPENSE', 'Sinh nhat, le tet', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, NULL, 'Chi khac', 'EXPENSE', 'Cac khoan chi khac', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, NULL, 'Luong', 'INCOME', 'Luong thang', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, NULL, 'Thuong', 'INCOME', 'Thuong KPI', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (13, NULL, 'Dau tu', 'INCOME', 'Lai dau tu', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (14, NULL, 'Ban hang', 'INCOME', 'Thu nhap kinh doanh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (15, NULL, 'Freelance', 'INCOME', 'Thu nhap lam them', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (16, NULL, 'Thu khac', 'INCOME', 'Cac khoan thu khac', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
