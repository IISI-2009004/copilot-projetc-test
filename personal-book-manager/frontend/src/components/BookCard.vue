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
  <div class="book-card" :class="{ 'book-card--selected': selected }" @click="$emit('select', book)">
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
        <el-tag
          v-for="tag in book.tags"
          :key="tag"
          size="small"
          round
          :color="tagColor(tag)"
          :style="tagColor(tag) ? { color: '#ffffff', border: 'none' } : undefined"
        >{{ tag }}</el-tag>
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
  border: 2px solid transparent;
}

.book-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.book-card--selected {
  border-color: var(--el-color-primary);
}

.book-card__cover {
  position: relative;
  aspect-ratio: 3 / 4;
  display: flex;
  align-items: center;
  justify-content: center;
}

.book-card__initial {
  font-size: 28px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.85);
}

.book-card__status {
  position: absolute;
  top: 6px;
  right: 6px;
}

.book-card__info {
  padding: 8px 10px 10px;
}

.book-card__title {
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-card__author {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin: 2px 0 6px;
}

.book-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
