import type { Category } from './types'

/** 分類樹狀共用工具：對應後端 Adjacency List 設計，前端以記憶體遞迴方式操作樹狀結構（無遞迴 SQL）。 */

/** 依 id 尋找節點（含子孫）。 */
export function findCategoryNode(nodes: Category[], id: string): Category | undefined {
  for (const node of nodes) {
    if (node.id === id) return node
    const found = node.children ? findCategoryNode(node.children, id) : undefined
    if (found) return found
  }
  return undefined
}

/** 依 id 尋找節點所在深度（頂層為 1）。 */
export function depthOfCategory(nodes: Category[], id: string, level = 1): number | undefined {
  for (const node of nodes) {
    if (node.id === id) return level
    const found = node.children ? depthOfCategory(node.children, id, level + 1) : undefined
    if (found) return found
  }
  return undefined
}

/** 尋找節點的父節點（頂層節點回傳 undefined）。 */
export function findCategoryParent(nodes: Category[], id: string, parent?: Category): Category | undefined {
  for (const node of nodes) {
    if (node.id === id) return parent
    if (node.children) {
      const found = findCategoryParent(node.children, id, node)
      if (found !== undefined) return found
      if (node.children.some((child) => child.id === id)) return node
    }
  }
  return undefined
}

/** 從樹中移除指定 id 節點，回傳是否成功移除。 */
export function removeCategoryNode(nodes: Category[], id: string): boolean {
  const index = nodes.findIndex((node) => node.id === id)
  if (index !== -1) {
    nodes.splice(index, 1)
    return true
  }
  for (const node of nodes) {
    if (node.children && removeCategoryNode(node.children, id)) return true
  }
  return false
}

/** 計算某節點的子孫節點數量（不含自己）。 */
export function countDescendants(node: Category): number {
  if (!node.children || node.children.length === 0) return 0
  return node.children.reduce((sum, child) => sum + 1 + countDescendants(child), 0)
}

/** 攤平樹狀分類為含深度資訊的陣列，供下拉選單／管理列表使用。 */
export function flattenCategories(nodes: Category[], depth = 1): Array<{ node: Category; depth: number }> {
  const result: Array<{ node: Category; depth: number }> = []
  for (const node of nodes) {
    result.push({ node, depth })
    if (node.children) result.push(...flattenCategories(node.children, depth + 1))
  }
  return result
}
