import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/books',
      name: 'books',
      // route level code-splitting：藏書管理頁面（開發中占位頁）
      component: () => import('../views/BookListView.vue'),
    },
    {
      path: '/reading',
      name: 'reading',
      // route level code-splitting：閱讀記錄頁面（開發中占位頁）
      component: () => import('../views/ReadingView.vue'),
    },
  ],
})

export default router
