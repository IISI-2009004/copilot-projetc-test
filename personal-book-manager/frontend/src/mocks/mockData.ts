import type { CurrentUser } from '@/types/user'
import type { ReadingStats } from '@/types/stats'

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
