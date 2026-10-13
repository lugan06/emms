import { createRouter, createWebHistory } from 'vue-router'

import { pinia } from '@/stores'
import { useUserStore } from '@/stores/user'
import { adminRoutes } from './admin'
import { userRoutes } from './user'
import { ElMessage } from 'element-plus'
import { isSuperAdmin } from '@/utils/system'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    userRoutes,
    adminRoutes,
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('@/views/admin/LoginView.vue'),
      meta: { guestOnly: true, title: 'Admin Login' },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: 'Not Found' },
    },
  ],
})

router.beforeEach(async (to) => {
  const userStore = useUserStore(pinia)

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return {
      name: 'admin-login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.requiresAuth && !userStore.userVerified) {
    try { await userStore.refreshUser() }
    catch {
      userStore.clearAuth()
      return { name: 'admin-login', query: { redirect: to.fullPath } }
    }
  }
  if (to.meta.requiresSuperAdmin && !isSuperAdmin(userStore.user)) {
    ElMessage.warning('无权限：仅超级管理员可查看操作日志')
    return { name: 'admin-dashboard' }
  }

  if (to.meta.guestOnly && userStore.isLoggedIn) {
    return { name: 'admin-dashboard' }
  }

  return true
})

export default router
