import axios from 'axios';
import { useAuthStore } from '../store/useAuthStore';

// Tạo một instance của axios với các cấu hình mặc định
const api = axios.create({
  baseURL: '/api', // URL gốc của Backend
  timeout: 10000, // Thời gian tối đa chờ API phản hồi (10 giây)
  headers: {
    'Content-Type': 'application/json',
  },
});

// --- REQUEST INTERCEPTOR ---
// Chạy trước mỗi lần Frontend gửi request lên Backend
api.interceptors.request.use(
  (config) => {
    // Lấy token trực tiếp từ Zustand store
    const token = useAuthStore.getState().token;
    
    // Nếu user đã đăng nhập và có token, tự động đính kèm vào Header
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// --- RESPONSE INTERCEPTOR ---
// Chạy sau khi Backend trả kết quả về cho Frontend
api.interceptors.response.use(
  (response) => {
    // Trả về trực tiếp response nếu thành công
    return response;
  },
  (error) => {
    // Nếu có lỗi, kiểm tra mã lỗi. Nếu là 401 (Unauthorized) nghĩa là Token sai/hết hạn
    if (error.response && error.response.status === 401) {
      // Gọi hàm logout trong store để xóa dữ liệu
      useAuthStore.getState().logout();
      
      // Đá người dùng văng ra trang đăng nhập
      window.location.href = '/auth/login';
    }
    
    return Promise.reject(error);
  }
);

export default api;
