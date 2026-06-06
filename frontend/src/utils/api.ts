import axios from 'axios';
import { useAuthStore } from '../store/useAuthStore';

const api = axios.create({
  baseURL: '/api', 
  timeout: 30000, 
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    if (error.response && error.response.status === 401 && !originalRequest?._retry) {
      const { refreshToken, user, roles, setAuth, logout } = useAuthStore.getState();

      if (refreshToken) {
        originalRequest._retry = true;
        try {
          const refreshResponse = await axios.post('/api/auth/refresh', { refreshToken });
          const data = refreshResponse.data?.data;
          if (data?.token) {
            setAuth(
              data.token,
              { email: data.email || user?.email, name: data.displayName || user?.name },
              data.roles || roles,
              data.refreshToken || refreshToken,
            );
            originalRequest.headers = originalRequest.headers || {};
            originalRequest.headers.Authorization = `Bearer ${data.token}`;
            return api(originalRequest);
          }
        } catch (refreshError) {
          logout();
          window.location.href = '/auth/login';
          return Promise.reject(refreshError);
        }
      }

      useAuthStore.getState().logout();
      window.location.href = '/auth/login';
    }
    
    return Promise.reject(error);
  }
);

export default api;
