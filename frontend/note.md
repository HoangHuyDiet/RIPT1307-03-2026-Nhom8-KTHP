# Cấu trúc thư mục Frontend (UmiJS)

`frontend/src/` là thư mục gốc chứa toàn bộ mã nguồn của ứng dụng React/UmiJS. Dưới đây là chức năng chính của từng thư mục:

```text
frontend/
├───mock                       # Chứa các file giả lập API (Mock API) dùng khi chưa có Backend thật.
│       auth.ts                # (Ví dụ: Mock API cho Đăng nhập / Đăng ký)
│
└───src/                       # Thư mục gốc chứa toàn bộ mã nguồn React/UmiJS.
    │   global.less            # File CSS/LESS toàn cục, chứa các reset CSS hoặc class dùng chung.
    │   
    ├───assets                 # Chứa các tài nguyên tĩnh (hình ảnh, icon, logo, font chữ,...).
    │       yay.jpg                
    │       
    ├───components             # Chứa các UI Components dùng chung (Button, Table, Modal...) để tái sử dụng.
    │
    ├───layouts                # Chứa các bộ khung giao diện (Layout) bọc ngoài các trang.
    │   ├───AuthLayout         # Layout dành riêng cho màn hình Đăng nhập / Đăng ký.
    │   │       index.less
    │   │       index.tsx
    │   │       
    │   └───BasicLayout        # Layout chính có chứa Sidebar, Header (Dành cho Dashboard).
    │           index.less
    │           index.tsx
    │
    ├───pages                  # Nơi chứa các trang (Pages). UmiJS tự động tạo Route dựa trên cấu trúc này.
    │   │   docs.tsx           # (Ví dụ route: /docs)
    │   │   index.tsx          # (Ví dụ route: / - Trang chủ gốc)
    │   │
    │   ├───auth               # Nhóm tính năng Xác thực tài khoản.
    │   │   ├───login          # Trang đăng nhập (Route: /auth/login)
    │   │   │       index.less
    │   │   │       index.tsx
    │   │   │
    │   │   └───register       # Trang đăng ký (Route: /auth/register)
    │   │           index.less
    │   │           index.tsx
    │   │
    │   └───dashboard          # Nhóm tính năng Quản lý tổng quan / Thống kê.
    │           index.tsx      # (Route: /dashboard)
    │
    ├───services               # Chứa các hàm dùng để gọi API tương tác với Backend thật.
    │
    ├───store                  # Chứa các file quản lý state toàn cục (Zustand/DvaJS).
    │       useAuthStore.ts    # Store lưu trữ thông tin User và Token sau khi đăng nhập.
    │
    ├───utils                  # Chứa các hàm tiện ích (helpers) dùng chung.
    │       api.ts             # File cấu hình Axios, tự động gắn Token và xử lý lỗi 401.
    │
    └───wrappers               # Chứa các Component bọc ngoài Route để kiểm tra điều kiện (ví dụ: Auth).
            AuthWrapper.tsx    # Wrapper kiểm tra xem User đã đăng nhập chưa, nếu chưa sẽ đẩy ra Login.
```

Thư viện cần tải (nhớ cd vào frontend trước khi chạy lệnh): `npx @ant-design/icons-react`, `npm install zustand`, `npm install axios`
Lệnh chạy dự án: `npm run dev`
Hướng pt nếu còn nhiều tg: 
- Bổ sung Conditional Rendering (Hiển thị có điều kiện)
- Thêm khả năng đăng nhập từ gg, apple
- Thêm giao diện cho phần xác nhận 2 lớp để dùng chung cho cả đăng ký, đăng nhập