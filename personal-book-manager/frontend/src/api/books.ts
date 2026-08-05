import { apiPost } from '@/utils/http'
import type { BookCreateRequest, BookResponse } from '@/types/bookApi'

/**
 * book 模組 API 呼叫（PG2：新增書本）。
 *
 * <p>對應後端 {@code POST /api/books}（design.md 5a / tasks.md A3）。
 */
export function createBook(request: BookCreateRequest): Promise<BookResponse> {
  return apiPost<BookResponse>('/books', request)
}
