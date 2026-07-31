<script setup lang="ts">
import { computed } from 'vue'
import type { Book } from '@/types/book'
import { BOOK_STATUS_LABEL } from '@/types/book'

const props = defineProps<{ book: Book }>()

/** 尚無真實封面圖時，以標題首字 + 依 id 循環的漸層色塊做縮圖佔位（模仿 Eagle 縮圖網格畫廊風格）。 */
const palettes: Array<[string, string]> = [
  ['#667eea', '#764ba2'],
  ['#f093fb', '#f5576c'],
  ['#4facfe', '#00f2fe'],
  ['#43e97b', '#38f9d7'],
  ['#fa709a', '#fee140'],
  ['#30cfd0', '#330867'],
]

const gradient = computed(() => {
  const [from, to] = palettes[props.book.id % palettes.length] ?? palettes[0]!
  return `linear-gradient(135deg, ${from}, ${to})`
})

const statusType = computed(() => {
  if (props.book.status === 'done') return 'success'
  if (props.book.status === 'reading') return 'warning'
  return 'info'
})
</script>

<template>
  <div class="book-card">
    <div class="book-card__cover" :style="{ background: gradient }">
      <span class="book-card__initial">{{ book.title.charAt(0) }}</span>
      <el-tag class="book-card__status" size="small" :type="statusType" effect="dark">
        {{ BOOK_STATUS_LABEL[book.status] }}
      </el-tag>
    </div>
    <div class="book-card__info">
      <div class="book-card__title" :title="book.title">{{ book.title }}</div>
      <div class="book-card__author">{{ book.author }}</div>
      <div class="book-card__tags">
        <el-tag v-for="tag in book.tags" :key="tag" size="small" round>{{ tag }}</el-tag>
      </div>
    </div>
  </div>
</template>

<style scoped>
.book-card {
  border-radius: 10px;
  overflow: hidden;
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.book-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.book-card__cover {
  position: relative;
  aspect-ratio: 3 / 4;
  display: flex;
  align-items: center;
  justify-content: center;
}

.book-card__initial {
  font-size: 40px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.85);
}

.book-card__status {
  position: absolute;
  top: 8px;
  right: 8px;
}

.book-card__info {
  padding: 10px 12px 12px;
}

.book-card__title {
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-card__author {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin: 2px 0 8px;
}

.book-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
