import type { RouteRecordRaw } from 'vue-router'

export const userRoutes: RouteRecordRaw = {
  path: '/',
  component: () => import('@/layouts/PublicLayout.vue'),
  children: [
    {
      path: 'contact',
      name: 'public-contact',
      component: () => import('@/views/user/ContactView.vue'),
      meta: { title: '联系我们' },
    },
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
    {
      path: 'category/:code',
      name: 'public-category-content',
      component: () => import('@/views/user/CategoryContentView.vue'),
      meta: { title: 'Category Content' },
    },
    {
      path: 'content/:id(\\d+)',
      name: 'public-content-detail',
      component: () => import('@/views/user/ContentView.vue'),
      meta: { title: 'Content Detail' },
    },
  ],
}
