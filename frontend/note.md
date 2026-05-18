# Cấu trúc thư mục Frontend (UmiJS)

`frontend/src/` là thư mục gốc chứa toàn bộ mã nguồn của ứng dụng React/UmiJS. Dưới đây là chức năng chính của từng thư mục:

```text
frontend/src/
│   global.less                # File CSS/LESS toàn cục, chứa các reset CSS hoặc class dùng chung cho toàn dự án.
│   
├───assets                     # Chứa các tài nguyên tĩnh (hình ảnh, icon, logo, font chữ,...).
│       yay.jpg                
│       
├───components                 # Chứa các UI Components dùng chung (Button, Table, Modal, Card...) để tái sử dụng ở nhiều trang khác nhau.
│
├───layouts                    # Chứa các bộ khung giao diện (Layout) bọc ngoài các trang để tạo sự nhất quán.
│   ├───AuthLayout             # Layout trống (Không Menu/Sidebar), dành riêng cho màn hình Đăng nhập / Đăng ký.
│   │       index.less
│   │       index.tsx
│   │       
│   └───BasicLayout            # Layout chính có chứa Sidebar, Header (Dành cho Dashboard và các trang quản lý tài chính).
│           index.less
│           index.tsx
│
├───pages                      # Nơi chứa các trang (Pages) của ứng dụng. UmiJS tự động tạo Route (Đường dẫn) dựa trên cấu trúc thư mục ở đây.
│   │   docs.tsx               # (Ví dụ route: /docs)
│   │   index.tsx              # (Ví dụ route: / - Trang chủ gốc)
│   │
│   ├───auth                   # Nhóm tính năng Xác thực tài khoản.
│   │   ├───login              # Trang đăng nhập (Route: /auth/login)
│   │   │       index.less
│   │   │       index.tsx
│   │   │
│   │   └───register           # Trang đăng ký (Route: /auth/register)
│   │           index.less
│   │           index.tsx
│   │
│   └───dashboard              # Nhóm tính năng Quản lý tổng quan / Thống kê.
│           index.tsx          # (Route: /dashboard)
│
├───services                   # Chứa các hàm dùng để gọi API tương tác với Backend (ví dụ: authService, userService...).
│
└───utils                      # Chứa các hàm tiện ích (helpers) dùng chung (ví dụ: hàm format ngày tháng, format tiền tệ, xử lý chuỗi...).
```

Thư viện cần tải: `npx @ant-design/icons-react`
Lệnh chạy dự án: `npm run dev`
