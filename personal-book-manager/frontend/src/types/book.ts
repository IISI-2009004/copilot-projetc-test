/**
 * 書本卡片顯示用型別（對應後端 book 模組，Scaffold 階段先以 mock 資料呈現封面縮圖畫廊）。
 */
export interface Book {
  id: number
  title: string
  author: string
  /** 封面圖片網址；Scaffold 階段尚無真實圖片，未提供時以標題首字 + 漸層色塊呈現縮圖佔位。 */
  coverUrl?: string
  category: string
  tags: string[]
  status: 'unread' | 'reading' | 'done'
}

export const BOOK_STATUS_LABEL: Record<Book['status'], string> = {
  unread: '未讀',
  reading: '閱讀中',
  done: '已完成',
}
