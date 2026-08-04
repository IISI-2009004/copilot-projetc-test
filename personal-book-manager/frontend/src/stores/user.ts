import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { CurrentUser } from '@/types/user'
import { mockCurrentUser } from '@/mocks/mockData'

/**
 * 使用者登入狀態 Store。
 *
 * Scaffold 階段：預設以 mock 使用者模擬「已登入」狀態，供首頁/Menu/使用者資訊
 * 元件開發版面使用；待後端 /api/auth/login、/api/users/me 完成後，
 * 改由實際 API 呼叫填入 currentUser 並移除 mock 資料依賴。
 */
export const useUserStore = defineStore('user', () => {
  const currentUser = ref<CurrentUser | null>(mockCurrentUser)

  function logout() {
    currentUser.value = null
  }

  return { currentUser, logout }
})
