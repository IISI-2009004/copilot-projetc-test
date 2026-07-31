<script setup lang="ts">
import { computed, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import BookCard from '@/components/BookCard.vue'
import { mockBooks } from '@/mocks/mockData'

const keyword = ref('')

/** 篩選邏輯純前端 mock 實作，待後端 GET /api/books?keyword= 完成後改為呼叫真實 API。 */
const filteredBooks = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return mockBooks
  return mockBooks.filter(
    (book) =>
      book.title.toLowerCase().includes(kw) ||
      book.author.toLowerCase().includes(kw) ||
      book.tags.some((tag) => tag.toLowerCase().includes(kw)),
  )
})
</script>

<template>
  <div class="book-list-view">
    <div class="book-list-view__toolbar">
      <h2 class="book-list-view__title">全部藏書</h2>
      <el-input
        v-model="keyword"
        placeholder="搜尋書名 / 作者 / 標籤"
        :prefix-icon="Search"
        clearable
        class="book-list-view__search"
      />
    </div>

    <el-empty v-if="filteredBooks.length === 0" description="找不到符合條件的書籍" />
    <div v-else class="book-list-view__grid">
      <BookCard v-for="book in filteredBooks" :key="book.id" :book="book" />
    </div>
  </div>
</template>

<style scoped>
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
}

.book-list-view__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}
</style>

