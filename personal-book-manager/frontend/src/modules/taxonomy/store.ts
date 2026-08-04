import { reactive } from 'vue'
import type { Category, Tag } from './types'

/**
 * Taxonomy 模組資料來源（Scaffold 階段 mock，之後由 src/services/* 呼叫真實 API 取代）。
 * 對外僅透過 index.ts 匯出的介面存取，避免其他模組直接操作內部陣列結構。
 */

/**
 * 分類樹狀階層（對應後端 Category 資料表，Adjacency List：parentId 自我參照，最多 3 層）。
 * 使用者可自由新增節點、指定顏色與父分類。
 */
export const mockCategories: Category[] = reactive([
  {
    id: 'tech',
    label: '技術',
    color: '#409eff',
    children: [
      { id: 'tech-frontend', label: '前端', color: '#67c23a' },
      { id: 'tech-backend', label: '後端', color: '#e6a23c' },
    ],
  },
  {
    id: 'literature',
    label: '文學',
    color: '#f56c6c',
    children: [
      { id: 'literature-novel', label: '小說' },
      { id: 'literature-essay', label: '散文' },
    ],
  },
  { id: 'business', label: '商業/理財', color: '#909399' },
  { id: 'self-growth', label: '自我成長' },
])

/**
 * Tag 清單（對應後端獨立的 Tag 資料表，扁平清單、可自由新增，各自可設定顏色）。
 * Book.tags 目前以名稱字串陣列儲存，顏色顯示時查詢本清單取得對應 color。
 */
export const mockTags: Tag[] = reactive([
  { id: 't1', name: '程式設計', color: '#409eff' },
  { id: 't2', name: '必讀', color: '#f56c6c' },
  { id: 't3', name: '架構', color: '#909399' },
  { id: 't4', name: '資料庫', color: '#e6a23c' },
  { id: 't5', name: '療癒', color: '#67c23a' },
  { id: 't6', name: '習慣養成' },
  { id: 't7', name: '暢銷', color: '#f56c6c' },
  { id: 't8', name: '投資' },
  { id: 't9', name: '思維模型' },
  { id: 't10', name: '專注力' },
  { id: 't11', name: '歷史' },
  { id: 't12', name: '重構' },
])
