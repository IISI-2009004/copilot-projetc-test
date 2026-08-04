<script setup lang="ts">
import { computed, ref } from 'vue'
import { Notebook, Timer, CircleCheck, Calendar } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { mockReadingStats, mockBooks } from '@/mocks/mockData'
import BookCard from '@/components/BookCard.vue'
import BookDetailPanel from '@/components/BookDetailPanel.vue'
import type { Book } from '@/types/book'

const userStore = useUserStore()
const stats = mockReadingStats
const recentBooks = computed(() => mockBooks.slice(0, 6))
/** 目前點選的書籍（參考 Eagle 點選項目後於右側顯示詳細資訊面板）。 */
const selectedBook = ref<Book | null>(null)

function selectBook(book: Book) {
  selectedBook.value = selectedBook.value?.id === book.id ? null : book
}

const greeting = computed(() => `歡迎回來，${userStore.currentUser?.username ?? '訪客'}`)

const cards = [
  { label: '藏書總數', value: stats.totalBooks, unit: '本', icon: Notebook },
  { label: '累計閱讀時長', value: stats.totalMinutes, unit: '分鐘', icon: Timer },
  { label: '已完成書籍', value: stats.completedBookCount, unit: '本', icon: CircleCheck },
  { label: '本月閱讀時長', value: stats.monthlyReadingMinutes, unit: '分鐘', icon: Calendar },
]
</script>

<template>
  <div class="home-view">
    <h2 class="home-view__greeting">{{ greeting }}</h2>
    <p class="home-view__hint">
      以下統計資料與封面畫廊為前端 Scaffold 階段的 mock 資料，待後端 API 完成後將改接真實數據。
    </p>

    <div class="home-view__cards">
      <el-tooltip v-for="card in cards" :key="card.label" :content="card.label" placement="top">
        <div class="stat-card">
          <el-icon class="stat-card__icon"><component :is="card.icon" /></el-icon>
          <div class="stat-card__value">{{ card.value }}<span class="stat-card__unit">{{ card.unit }}</span></div>
        </div>
      </el-tooltip>
    </div>

    <div class="home-view__section">
      <div class="home-view__section-header">
        <h3>最近加入</h3>
        <RouterLink to="/books" class="home-view__more">查看全部藏書 &gt;</RouterLink>
      </div>
      <div class="home-view__grid">
        <BookCard
          v-for="book in recentBooks"
          :key="book.id"
          :book="book"
          :selected="selectedBook?.id === book.id"
          @select="selectBook"
        />
      </div>
    </div>

    <Teleport to="#detail-panel-outlet">
      <BookDetailPanel v-if="selectedBook" :book="selectedBook" @close="selectedBook = null" />
    </Teleport>
  </div>
</template>

<style scoped>
.home-view__greeting {
  margin: 0 0 4px;
}

.home-view__hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin: 0 0 20px;
}

.home-view__cards {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  white-space: nowrap;
}

.stat-card__icon {
  font-size: 20px;
  color: var(--el-color-primary);
}

.stat-card__value {
  font-size: 16px;
  font-weight: 600;
}

.stat-card__unit {
  font-size: 12px;
  font-weight: 400;
  margin-left: 2px;
  color: var(--el-text-color-secondary);
}

.home-view__section {
  margin-top: 12px;
}

.home-view__section-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
}

.home-view__section-header h3 {
  margin: 0;
}

.home-view__more {
  font-size: 13px;
  color: var(--el-color-primary);
  text-decoration: none;
}

.home-view__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}
</style>

