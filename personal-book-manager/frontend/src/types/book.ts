/**
 * 書本卡片顯示用型別（對應後端 book 模組，Scaffold 階段先以 mock 資料呈現封面縮圖畫廊）。
 */
export interface Book {
  id: number
  title: string
  author: string
  /** 封面圖片網址；Scaffold 階段尚無真實圖片，未提供時以標題首字 + 漸層色塊呈現縮圖佔位。 */
  coverUrl?: string
  /** 所屬分類；未指定代表「未分類」（對應側邊欄固定項目）。 */
  category?: string
  /** 標籤清單；空陣列代表「未標籤」（對應側邊欄固定項目）。 */
  tags: string[]
  status: 'unread' | 'reading' | 'done'
  /** ISBN；Scaffold 階段選填，供右側詳細資訊面板顯示。 */
  isbn?: string
  /** 簡介／筆記；供右側詳細資訊面板顯示。 */
  description?: string
  /** 加入書庫日期（ISO 字串）；供右側詳細資訊面板顯示。 */
  addedAt?: string
}

export const BOOK_STATUS_LABEL: Record<Book['status'], string> = {
  unread: '未讀',
  reading: '閱讀中',
  done: '已完成',
}
