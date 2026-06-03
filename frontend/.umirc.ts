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
        { path: '/auth/change-password', component: '@/pages/auth/change-password' },
        { path: '/auth/otp', component: '@/pages/auth/otp' },
      ],
    },

    {
      path: '/',
      component: '@/layouts/BasicLayout',
      routes: [
        { path: '/', redirect: '/auth/login' },
        { path: '/dashboard', component: '@/pages/dashboard' },
        { path: '/personal-funds', component: '@/pages/personal-funds' },
        { path: '/funds', component: '@/pages/funds' },
        { path: '/funds/verify', component: '@/pages/funds/verify' },
        { path: '/saving-goals', component: '@/pages/saving-goals' },
        { path: '/transactions', component: '@/pages/transactions' },
        { path: '/categories', component: '@/pages/categories' },
      ],
    },

    {
      path: '/admin',
      component: '@/layouts/AdminLayout',
      routes: [
        { path: '/admin/users', component: '@/pages/admin/users' },
        { path: '/admin/categories', component: '@/pages/admin/categories' },
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
    '/ws': {
      target: 'http://localhost:8080/backend',
      changeOrigin: true,
      ws: true,
    },
  },
});
