import { computed, type Ref } from 'vue'
import type { Book } from '@/types/book'

/** 尚無真實封面圖時，以標題首字 + 依 id 循環的漸層色塊做縮圖佔位（模仿 Eagle 縮圖網格畫廊風格）。 */
const PALETTES: Array<[string, string]> = [
  ['#667eea', '#764ba2'],
  ['#f093fb', '#f5576c'],
  ['#4facfe', '#00f2fe'],
  ['#43e97b', '#38f9d7'],
  ['#fa709a', '#fee140'],
  ['#30cfd0', '#330867'],
]

/**
 * 依書籍 id 產生一致的漸層色塊，供 BookCard/BookListItem/BookDetailPanel 共用，
 * 避免同一份色票邏輯散落在多個元件中重複維護。
 */
export function useBookCoverGradient(book: Ref<Book>) {
  return computed(() => {
    const [from, to] = PALETTES[book.value.id % PALETTES.length] ?? PALETTES[0]!
    return `linear-gradient(135deg, ${from}, ${to})`
  })
}
