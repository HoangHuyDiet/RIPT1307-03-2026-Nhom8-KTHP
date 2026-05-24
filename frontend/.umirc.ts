import { defineConfig } from "umi";

export default defineConfig({
  routes: [
    {
      path: '/auth',
      component: '@/layouts/AuthLayout',
      routes: [
        { path: '/auth/login', component: '@/pages/auth/login' },
        { path: '/auth/register', component: '@/pages/auth/register' },
        { path: '/auth/forgot-password', component: '@/pages/auth/forgot-password' },

        { path: '/auth/otp', component: '@/pages/auth/otp' },
      ],
    },

    {
      path: '/',
      component: '@/layouts/BasicLayout',
      routes: [
        { path: '/', redirect: '/auth/login' },
        { path: '/dashboard', component: '@/pages/dashboard' },
      ],
    },

    {
      path: '/admin',
      component: '@/layouts/AdminLayout',
      routes: [
        { path: '/admin/users', component: '@/pages/admin/users' },
      ],
    },
  ],
  mock: false,
  npmClient: 'npm',
  utoopack: {},
  proxy: {
    '/api': {
      target: 'http://localhost:8080/backend',
      changeOrigin: true,
    },
  },
});
