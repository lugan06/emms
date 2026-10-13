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
    {
      path: 'exhibitions',
      name: 'admin-exhibitions',
      component: () => import('@/views/admin/ExhibitionListView.vue'),
      meta: { title: '展会管理' },
    },
    {
      path: 'exhibitions/new',
      name: 'admin-exhibition-create',
      component: () => import('@/views/admin/ExhibitionEditView.vue'),
      meta: { title: '新增展会' },
    },
    {
      path: 'exhibitions/:id(\\d+)/edit',
      name: 'admin-exhibition-edit',
      component: () => import('@/views/admin/ExhibitionEditView.vue'),
      meta: { title: '编辑展会' },
    },
    {
      path: 'categories',
      name: 'admin-categories',
      component: () => import('@/views/admin/CategoryManagementView.vue'),
      meta: { title: '栏目管理' },
    },
    {
      path: 'contents',
      name: 'admin-contents',
      component: () => import('@/views/admin/ContentListView.vue'),
      meta: { title: '内容管理' },
    },
    {
      path: 'contents/new',
      name: 'admin-content-create',
      component: () => import('@/views/admin/ContentEditView.vue'),
      meta: { title: '新增内容' },
    },
    {
      path: 'contents/:id(\\d+)/edit',
      name: 'admin-content-edit',
      component: () => import('@/views/admin/ContentEditView.vue'),
      meta: { title: '编辑内容' },
    },
  ],
}
