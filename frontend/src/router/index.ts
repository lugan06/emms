import { createRouter, createWebHistory } from 'vue-router'

import { pinia } from '@/stores'
import { useUserStore } from '@/stores/user'
import { adminRoutes } from './admin'
import { userRoutes } from './user'

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

router.beforeEach((to) => {
  const userStore = useUserStore(pinia)

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return {
      name: 'admin-login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.guestOnly && userStore.isLoggedIn) {
    return { name: 'admin-dashboard' }
  }

  return true
})

export default router
