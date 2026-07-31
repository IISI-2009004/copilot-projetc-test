<script setup lang="ts">
import { computed, toRef } from 'vue'
import type { Book } from '@/types/book'
import { BOOK_STATUS_LABEL } from '@/types/book'
import { useBookCoverGradient } from '@/composables/useBookCoverGradient'
import { useTagColor } from '@/modules/taxonomy'

const props = defineProps<{ book: Book; selected?: boolean }>()
defineEmits<{ select: [book: Book] }>()

const gradient = useBookCoverGradient(toRef(props, 'book'))
const { tagColor } = useTagColor()

const statusType = computed(() => {
  if (props.book.status === 'done') return 'success'
  if (props.book.status === 'reading') return 'warning'
  return 'info'
})
</script>

<template>
  <div class="book-row" :class="{ 'book-row--selected': selected }" @click="$emit('select', book)">
    <div class="book-row__thumb" :style="{ background: gradient }">
      <span>{{ book.title.charAt(0) }}</span>
    </div>
    <div class="book-row__title" :title="book.title">{{ book.title }}</div>
    <div class="book-row__author">{{ book.author }}</div>
    <div class="book-row__category">{{ book.category ?? '未分類' }}</div>
    <div class="book-row__tags">
      <el-tag
        v-for="tag in book.tags"
        :key="tag"
        size="small"
        round
        :color="tagColor(tag)"
        :style="tagColor(tag) ? { color: '#ffffff', border: 'none' } : undefined"
      >{{ tag }}</el-tag>
    </div>
    <el-tag class="book-row__status" size="small" :type="statusType">
      {{ BOOK_STATUS_LABEL[book.status] }}
    </el-tag>
  </div>
</template>

<style scoped>
.book-row {
  display: grid;
  grid-template-columns: 44px 1.5fr 1fr 0.8fr 1.5fr 80px;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background-color: #ffffff;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.15s ease;
  border: 1px solid transparent;
}

.book-row:hover {
  background-color: var(--el-fill-color-light);
}

.book-row--selected {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

.book-row__thumb {
  width: 36px;
  height: 44px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 700;
  flex-shrink: 0;
}

.book-row__title {
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-row__author,
.book-row__category {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-row__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.book-row__status {
  justify-self: end;
}
</style>
