import { reactive } from 'vue'
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
 *
 * 註：Tag／Category 的 mock 資料已搬移至 `@/modules/taxonomy`（獨立模組），
 * 請改由 `import { mockCategories, mockTags } from '@/modules/taxonomy'` 取得。
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


/**
 * Scaffold 階段以 reactive 陣列模擬藏書資料，讓詳細資訊面板可直接編輯並即時反映到卡片/列表畫面。
 * 待後端 book 模組 API 完成後，改由 src/services/* 呼叫真實資料並移除本檔案。
 */
export const mockBooks: Book[] = reactive([
  { id: 1, title: 'Clean Code', author: 'Robert C. Martin', category: '前端', tags: ['程式設計', '必讀'], status: 'done', isbn: '978-0132350884', addedAt: '2026-01-20T08:00:00Z', description: '闡述如何撰寫易讀、易維護的程式碼，透過大量範例說明命名、函式、註解等實踐原則。' },
  { id: 2, title: 'Designing Data-Intensive Applications', author: 'Martin Kleppmann', category: '後端', tags: ['架構', '資料庫'], status: 'reading', isbn: '978-1449373320', addedAt: '2026-02-10T08:00:00Z', description: '深入探討大型資料系統的設計原則，涵蓋複寫、分區、交易與一致性等主題。' },
  { id: 3, title: '解憂雜貨店', author: '東野圭吾', category: '小說', tags: ['療癒'], status: 'done', addedAt: '2026-02-15T08:00:00Z', description: '透過書信往來串連時空的溫馨故事，療癒人心之作。' },
  { id: 4, title: '原子習慣', author: 'James Clear', category: '自我成長', tags: ['習慣養成', '暢銷'], status: 'reading', addedAt: '2026-03-01T08:00:00Z', description: '以微小習慣的複利效應，說明如何透過系統化方式建立長期改變。' },
  { id: 5, title: '窮查理的普通常識', author: '查理蒙格', category: '商業/理財', tags: ['投資', '思維模型'], status: 'unread', addedAt: '2026-03-12T08:00:00Z' },
  { id: 6, title: '深度工作力', author: 'Cal Newport', category: '自我成長', tags: ['專注力'], status: 'unread', addedAt: '2026-03-20T08:00:00Z' },
  { id: 7, title: '人類大歷史', author: '哈拉瑞', category: '散文', tags: ['歷史', '暢銷'], status: 'done', addedAt: '2026-04-02T08:00:00Z', description: '從認知革命、農業革命到科學革命，重新審視人類文明的發展脈絡。' },
  { id: 8, title: 'Refactoring', author: 'Martin Fowler', category: '後端', tags: ['程式設計', '重構'], status: 'unread', isbn: '978-0134757599', addedAt: '2026-04-18T08:00:00Z' },
  { id: 9, title: '未整理的書', author: '匿名', tags: [], status: 'unread', addedAt: '2026-04-20T08:00:00Z' },
  { id: 10, title: '剛買的新書', author: '匿名', category: '商業/理財', tags: [], status: 'unread', addedAt: '2026-04-25T08:00:00Z' },
])
