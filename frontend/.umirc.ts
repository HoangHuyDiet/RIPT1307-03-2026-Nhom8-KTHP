import { defineConfig } from "umi";

export default defineConfig({
  routes: [
    {
      path: '/auth',
      component: '@/layouts/AuthLayout',
      routes: [
        { path: '/auth/login', component: '@/pages/auth/login' },
        { path: '/auth/register', component: '@/pages/auth/register' },
      ],
    },

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
