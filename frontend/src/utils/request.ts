import axios from 'axios';
import { message } from 'antd';

const baseURL = process.env.API_URL || '/api';

const request = axios.create({
  baseURL,
  timeout: 30000, 
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

request.interceptors.response.use((response) => {
  return response.data;
}, (error) => {
  if (error.response?.status === 401) {
    message.error('Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!');
    localStorage.removeItem('access_token');
    window.location.href = '/auth/login';
  } else {
    message.error(error.response?.data?.message || 'Có lỗi xảy ra kết nối Server');
  }
  return Promise.reject(error);
});

export default request as any;
