/**
 * 對應後端 book 模組 API 的型別（design.md 5a / tasks.md A3）。
 *
 * <p>與 {@code types/book.ts} 的 {@code Book}（前端卡片顯示用型別）不同：本檔案的型別
 * 直接對應後端 {@code BookRequest}/{@code BookResponse} DTO 欄位，供 API 呼叫使用。
 */

export type BookType =
  | 'PHYSICAL_BOOK'
  | 'PHYSICAL_DOUJINSHI'
  | 'EBOOK'
  | 'WEB_NOVEL'
  | 'BLOG_POST'
  | 'ONLINE_FANFIC'

export const BOOK_TYPE_LABEL: Record<BookType, string> = {
  PHYSICAL_BOOK: '實體書籍',
  PHYSICAL_DOUJINSHI: '實體同人誌',
  EBOOK: '電子書',
  WEB_NOVEL: '網路小說',
  BLOG_POST: 'Blog 文章',
  ONLINE_FANFIC: '同人文網站作品',
}

/** 「實體／電子書」類：可填 isbn，不可填 url。 */
export const PHYSICAL_OR_EBOOK_TYPES: BookType[] = ['PHYSICAL_BOOK', 'PHYSICAL_DOUJINSHI', 'EBOOK']

/** 「線上內容」類：必填 url，不可填 isbn。 */
export const ONLINE_BOOK_TYPES: BookType[] = ['WEB_NOVEL', 'BLOG_POST', 'ONLINE_FANFIC']

export interface BookCreateRequest {
  isbn?: string | null
  url?: string | null
  sourcePlatform?: string | null
  title: string
  author: string
  bookType: BookType
  categoryId?: number | null
  purchaseUrl?: string | null
  authorUrl?: string | null
}

export interface BookResponse {
  id: number
  isbn?: string | null
  url?: string | null
  sourcePlatform?: string | null
  title: string
  author: string
  bookType: BookType
  categoryId?: number | null
  purchaseUrl?: string | null
  authorUrl?: string | null
  coverImageUrl?: string | null
  coverImageSource?: 'UPLOADED' | 'EXTERNAL_URL' | null
  createdAt: string
  updatedAt: string
}
