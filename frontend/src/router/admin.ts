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
      meta: { title: '控制台' },
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
    { path: 'site', name: 'admin-site', component: () => import('@/views/admin/SiteConfigView.vue'), meta: { title: '站点配置' } },
    { path: 'company', name: 'admin-company', component: () => import('@/views/admin/CompanyConfigView.vue'), meta: { title: '公司信息' } },
    { path: 'guestbooks', name: 'admin-guestbooks', component: () => import('@/views/admin/GuestbookListView.vue'), meta: { title: '留言管理' } },
    { path: 'logs', name: 'admin-logs', component: () => import('@/views/admin/OperationLogView.vue'), meta: { title: '日志管理', requiresSuperAdmin: true } },
  ],
}
