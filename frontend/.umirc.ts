import { defineConfig } from "umi";

export default defineConfig({
  routes: [
    // NHÁNH 1: Các trang Xác thực (Sử dụng AuthLayout)
    {
      path: '/auth',
      component: '@/layouts/AuthLayout',
      routes: [
        { path: '/auth/login', component: '@/pages/auth/login' },
        { path: '/auth/register', component: '@/pages/auth/register' },
      ],
    },

    // NHÁNH 2: Các trang Chính của ứng dụng (Sử dụng BasicLayout)
    {
      path: '/',
      component: '@/layouts/BasicLayout',
      routes: [
        { path: '/', redirect: '/dashboard' },
        { path: '/dashboard', component: '@/pages/dashboard' },
      ],
    },
  ],
  npmClient: 'npm',
  utoopack: {},
});
