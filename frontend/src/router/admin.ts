import type { RouteRecordRaw } from 'vue-router'

export const adminRoutes: RouteRecordRaw = {
  path: '/admin',
  component: () => import('@/layouts/AdminLayout.vue'),
  meta: { requiresAuth: true },
  children: [
    {
      path: '',
      name: 'admin-dashboard',
      component: () => import('@/views/admin/DashboardView.vue'),
      meta: { title: 'Dashboard' },
    },
  ],
}
