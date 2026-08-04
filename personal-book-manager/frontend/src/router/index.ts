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
      // route level code-splitting：藏書列表頁（支援 query: filter=uncategorized|untagged、category=分類名稱）
      component: () => import('../views/BookListView.vue'),
    },
    {
      path: '/tags',
      name: 'tags',
      // route level code-splitting：taxonomy 模組 - 標籤管理頁面
      component: () => import('../modules/taxonomy/views/TagManagementView.vue'),
    },
    {
      path: '/categories',
      name: 'categories',
      // route level code-splitting：taxonomy 模組 - 分類管理頁面（樹狀階層 CRUD）
      component: () => import('../modules/taxonomy/views/CategoryManagementView.vue'),
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
