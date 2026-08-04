/**
 * Taxonomy 模組公開介面（Tag ＋ Category）。
 * 其他模組（如 book）僅能透過此檔案匯出的型別／資料／工具存取 taxonomy 內部狀態，
 * 不應直接 import `./store` 或內部檔案，以維持模組邊界（比照後端 book 模組對外只暴露
 * `BookQueryPort` 的精神）。
 */
export type { Category, Tag } from './types'
export { CATEGORY_MAX_DEPTH } from './types'

export { mockCategories, mockTags } from './store'

export {
  findCategoryNode,
  depthOfCategory,
  findCategoryParent,
  removeCategoryNode,
  countDescendants,
  flattenCategories,
} from './categoryTree'

export { useTagColor } from './useTagColor'
