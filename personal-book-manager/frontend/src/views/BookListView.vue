<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search, Grid, List } from '@element-plus/icons-vue'
import BookCard from '@/components/BookCard.vue'
import BookListItem from '@/components/BookListItem.vue'
import BookDetailPanel from '@/components/BookDetailPanel.vue'
import { mockBooks } from '@/mocks/mockData'
import type { Book } from '@/types/book'

type ViewMode = 'card' | 'list'

const route = useRoute()
const keyword = ref('')
/** 顯示模式：卡片縮圖網格 或 條列清單；記住使用者選擇，重新整理頁面前維持一致。 */
const viewMode = ref<ViewMode>((localStorage.getItem('book-list-view-mode') as ViewMode) || 'card')
/** 目前點選的書籍（參考 Eagle 點選項目後於右側顯示詳細資訊面板）。 */
const selectedBook = ref<Book | null>(null)

function setViewMode(mode: ViewMode) {
  viewMode.value = mode
  localStorage.setItem('book-list-view-mode', mode)
}

function selectBook(book: Book) {
  selectedBook.value = selectedBook.value?.id === book.id ? null : book
}

/** 側邊欄固定分類／分類樹狀點選後，透過 query（filter=uncategorized|untagged、category=分類名稱）帶入本頁。 */
const pageTitle = computed(() => {
  if (route.query.filter === 'uncategorized') return '未分類'
  if (route.query.filter === 'untagged') return '未標籤'
  if (typeof route.query.category === 'string') return route.query.category
  return '全部藏書'
})

/** 篩選邏輯純前端 mock 實作，待後端 GET /api/books?keyword=&category=&filter= 完成後改為呼叫真實 API。 */
const filteredBooks = computed(() => {
  let books = mockBooks
  if (route.query.filter === 'uncategorized') {
    books = books.filter((book) => !book.category)
  } else if (route.query.filter === 'untagged') {
    books = books.filter((book) => book.tags.length === 0)
  } else if (typeof route.query.category === 'string') {
    books = books.filter((book) => book.category === route.query.category)
  }

  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return books
  return books.filter(
    (book) =>
      book.title.toLowerCase().includes(kw) ||
      book.author.toLowerCase().includes(kw) ||
      book.tags.some((tag) => tag.toLowerCase().includes(kw)),
  )
})

// 切換分類/篩選條件時，清除舊的選取狀態，避免右側面板顯示已不在清單中的書籍。
watch([() => route.query.filter, () => route.query.category], () => {
  selectedBook.value = null
})
</script>

<template>
  <div class="book-list-view">
    <div class="book-list-view__main">
      <div class="book-list-view__toolbar">
        <h2 class="book-list-view__title">{{ pageTitle }}</h2>
        <el-input
          v-model="keyword"
          placeholder="搜尋書名 / 作者 / 標籤"
          :prefix-icon="Search"
          clearable
          class="book-list-view__search"
        />
        <el-radio-group :model-value="viewMode" class="book-list-view__switch" @update:model-value="setViewMode">
          <el-radio-button value="card">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button value="list">
            <el-icon><List /></el-icon>
          </el-radio-button>
        </el-radio-group>
      </div>

      <el-empty v-if="filteredBooks.length === 0" description="找不到符合條件的書籍" />
      <div v-else-if="viewMode === 'card'" class="book-list-view__grid">
        <BookCard
          v-for="book in filteredBooks"
          :key="book.id"
          :book="book"
          :selected="selectedBook?.id === book.id"
          @select="selectBook"
        />
      </div>
      <div v-else class="book-list-view__list">
        <BookListItem
          v-for="book in filteredBooks"
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
.book-list-view {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.book-list-view__main {
  flex: 1;
  min-width: 0;
}

.book-list-view__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.book-list-view__title {
  margin: 0;
  white-space: nowrap;
}

.book-list-view__search {
  max-width: 320px;
  flex: 1;
}

.book-list-view__switch {
  flex-shrink: 0;
}

.book-list-view__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.book-list-view__list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
</style>


