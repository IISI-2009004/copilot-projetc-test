import { mockTags } from './store'

/** 依標籤名稱查詢 mockTags 註冊表取得顯示顏色（對應後端獨立 Tag 資料表），供 book 模組元件共用。 */
export function useTagColor() {
  function tagColor(name: string): string | undefined {
    return mockTags.find((t) => t.name === name)?.color
  }
  return { tagColor }
}
