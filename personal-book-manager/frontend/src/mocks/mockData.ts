import type { CurrentUser } from '@/types/user'
import type { ReadingStats } from '@/types/stats'
import type { Book } from '@/types/book'

/**
 * 前端 Scaffold 階段暫用的假資料。
 *
 * 後端 user/book/reading 模組（GET /api/users/me、GET /api/reading/stats 等）
 * 目前僅有端點骨架（拋出 UnsupportedOperationException，尚未實作商業邏輯），
 * 因此本階段前端頁面（首頁／Menu／使用者資訊）先以本機 mock 資料建置版面，
 * 待後端功能模組開發完成後，再以 src/services/* 的真實 API 呼叫取代這裡的資料來源。
 */
export const mockCurrentUser: CurrentUser = {
  id: 1,
  username: 'demo_user',
  createdAt: '2026-01-15T09:00:00Z',
}

export const mockReadingStats: ReadingStats = {
  totalBooks: 12,
  totalMinutes: 3260,
  completedBookCount: 5,
  monthlyReadingMinutes: 480,
}

/** 分類樹狀結構（對應 Eagle 側邊欄的資料夾樹狀導覽），Scaffold 階段暫用假資料。 */
export const mockCategories: Array<{ id: string; label: string; children?: Array<{ id: string; label: string }> }> = [
  {
    id: 'tech',
    label: '技術',
    children: [
      { id: 'tech-frontend', label: '前端' },
      { id: 'tech-backend', label: '後端' },
    ],
  },
  {
    id: 'literature',
    label: '文學',
    children: [
      { id: 'literature-novel', label: '小說' },
      { id: 'literature-essay', label: '散文' },
    ],
  },
  { id: 'business', label: '商業/理財' },
  { id: 'self-growth', label: '自我成長' },
]

export const mockBooks: Book[] = [
  { id: 1, title: 'Clean Code', author: 'Robert C. Martin', category: '前端', tags: ['程式設計', '必讀'], status: 'done' },
  { id: 2, title: 'Designing Data-Intensive Applications', author: 'Martin Kleppmann', category: '後端', tags: ['架構', '資料庫'], status: 'reading' },
  { id: 3, title: '解憂雜貨店', author: '東野圭吾', category: '小說', tags: ['療癒'], status: 'done' },
  { id: 4, title: '原子習慣', author: 'James Clear', category: '自我成長', tags: ['習慣養成', '暢銷'], status: 'reading' },
  { id: 5, title: '窮查理的普通常識', author: '查理蒙格', category: '商業/理財', tags: ['投資', '思維模型'], status: 'unread' },
  { id: 6, title: '深度工作力', author: 'Cal Newport', category: '自我成長', tags: ['專注力'], status: 'unread' },
  { id: 7, title: '人類大歷史', author: '哈拉瑞', category: '散文', tags: ['歷史', '暢銷'], status: 'done' },
  { id: 8, title: 'Refactoring', author: 'Martin Fowler', category: '後端', tags: ['程式設計', '重構'], status: 'unread' },
]
