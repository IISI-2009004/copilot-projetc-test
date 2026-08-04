/**
 * 對應後端 UserResponse DTO（com.iisi.bookmanager.user.dto.UserResponse）。
 * 刻意不含密碼欄位，避免密碼雜湊值經前端外洩。
 */
export interface CurrentUser {
  id: number
  username: string
  createdAt: string
}
