import type { RouteRecordRaw } from 'vue-router'

export const userRoutes: RouteRecordRaw = {
  path: '/',
  component: () => import('@/layouts/PublicLayout.vue'),
  children: [
    {
      path: '',
      name: 'user-home',
      component: () => import('@/views/user/HomeView.vue'),
      meta: { title: 'Home' },
    },
    {
      path: 'exhibitions',
      name: 'public-exhibitions',
      component: () => import('@/views/user/ExhibitionListView.vue'),
      meta: { title: 'Exhibitions' },
    },
    {
      path: 'exhibitions/:id(\\d+)',
      name: 'public-exhibition-detail',
      component: () => import('@/views/user/ExhibitionDetailView.vue'),
      meta: { title: 'Exhibition Detail' },
    },
  ],
}
