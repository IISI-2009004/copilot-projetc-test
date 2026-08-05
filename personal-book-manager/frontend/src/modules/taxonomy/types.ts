/**
 * Taxonomy 模組（標籤 Tag ＋ 分類 Category）共用型別。
 * 對應後端獨立的 Tag / Category 資料表：兩者皆可自訂顯示顏色，但僅 Category 具備樹狀階層（parentId 自我參照）。
 */

export interface Category {
  id: string
  label: string
  /** 顯示顏色（`#RRGGBB`）；未設定時前端以預設灰階呈現。 */
  color?: string
  children?: Category[]
}

/** 分類階層深度上限（對應後端 CategoryDepthExceededException 的驗證規則）。 */
export const CATEGORY_MAX_DEPTH = 3

export interface Tag {
  id: string
  name: string
  /** 顯示顏色（`#RRGGBB`）；未設定時沿用 Element Plus 預設標籤樣式。 */
  color?: string
}
