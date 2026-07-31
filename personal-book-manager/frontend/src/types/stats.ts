/**
 * 首頁閱讀統計卡片資料。
 * 對應後端 reading 模組 US-R05：總閱讀時長（分鐘）、已完成書籍數。
 */
export interface ReadingStats {
  totalBooks: number
  totalMinutes: number
  completedBookCount: number
  monthlyReadingMinutes: number
}
