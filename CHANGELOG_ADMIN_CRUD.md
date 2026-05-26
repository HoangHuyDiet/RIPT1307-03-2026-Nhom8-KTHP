# Changelog: Cập Nhật Tính Năng Quản Lý Người Dùng (Admin CRUD)

## Backend (Spring Boot)
- **DataInitializer**: Cập nhật cơ chế tự động tạo tài khoản `ADMIN` và `SUPPORT_ADMIN` với hash password chuẩn từ `PasswordEncoder`, khắc phục lỗi đăng nhập do dùng bcrypt hash cứng sai cấu trúc.
- **AdminController**: Thêm đầy đủ bộ API RESTful quản lý người dùng:
  - `GET /api/admin/users/{id}`: Lấy thông tin người dùng theo ID.
  - `POST /api/admin/users`: Tạo tài khoản mới (cấp quyền và đặt password).
  - `PUT /api/admin/users/{id}`: Cập nhật thông tin và roles của người dùng.
  - `DELETE /api/admin/users/{id}`: Xóa vĩnh viễn tài khoản (đã chặn tự xóa quyền ADMIN).
- **AdminService & AdminServiceImpl**: Thêm logic xử lý cho toàn bộ các API trên, kèm theo việc quản lý xóa cascade bảng `user_role` liên quan.
- **DTOs**: Thêm `AdminCreateUserRequest` và `AdminUpdateUserRequest` để handle validation từ form phía client.

## Frontend (React + Ant Design)
- **Trang Quản lý Người Dùng (`pages/admin/users/index.tsx`)**:
  - Gắn thêm 2 nút **Sửa** (Edit) và **Xóa** (Delete) vào cột hành động trên bảng.
  - Xây dựng cửa sổ **Modal** chứa Form linh hoạt để Tạo mới và Sửa đổi thông tin tài khoản (đổi Tên, đổi Email, cập nhật Role, đổi Trạng Thái, cập nhật Mật Khẩu).
  - Tích hợp các hàm gọi trực tiếp API `api.post`, `api.put`, `api.delete`.
  - Tự động chặn các thao tác (ẩn UI) nếu tài khoản đang đăng nhập chỉ là `SUPPORT_ADMIN`.
- **Styling (`index.less`)**:
  - Bổ sung các class CSS `.editBtn`, `.deleteBtn` hỗ trợ hiệu ứng hiển thị thân thiện, đúng chuẩn màu cảnh báo.

## Tác Động
- Tính năng phân quyền (RBAC) giờ đã hoàn chỉnh luồng nghiệp vụ.
- Quản trị viên (Admin) không còn cần phải thao tác thủ công với database bằng SQL script nữa, có thể chủ động kiểm soát tài khoản người dùng trực tiếp qua giao diện web.
