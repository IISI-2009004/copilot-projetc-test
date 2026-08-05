<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Files, FolderOpened, Notebook, PriceTag, FolderRemove, Remove, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { mockBooks } from '@/mocks/mockData'
import { mockCategories, CATEGORY_MAX_DEPTH, type Category, findCategoryNode, depthOfCategory } from '@/modules/taxonomy'
import UserInfoWidget from '@/components/UserInfoWidget.vue'

const route = useRoute()
const router = useRouter()

/** 固定分類（對應 Eagle 側邊欄的「全部/未分類/未標籤/標籤管理」），項目後方顯示統計數字。 */
const uncategorizedCount = computed(() => mockBooks.filter((book) => !book.category).length)
const untaggedCount = computed(() => mockBooks.filter((book) => book.tags.length === 0).length)
const totalCount = computed(() => mockBooks.length)

/** 依分類名稱統計書籍數量（含子分類），供分類樹節點顯示統計數字使用。 */
function countByCategory(node: Category): number {
  const own = mockBooks.filter((book) => book.category === node.label).length
  const childSum = node.children?.reduce((sum, child) => sum + countByCategory(child), 0) ?? 0
  return own + childSum
}

const activeQuickFilter = computed(() => {
  if (route.path !== '/books') return ''
  if (route.query.filter === 'uncategorized') return 'uncategorized'
  if (route.query.filter === 'untagged') return 'untagged'
  if (!route.query.filter && !route.query.category) return 'all'
  return ''
})

function goToQuickFilter(filter: 'all' | 'uncategorized' | 'untagged') {
  if (filter === 'all') {
    router.push({ path: '/books' })
  } else {
    router.push({ path: '/books', query: { filter } })
  }
}

function goToCategory(label: string) {
  router.push({ path: '/books', query: { category: label } })
}

/**
 * 分類樹狀階層管理（對應後端 Category 的 Adjacency List 設計）：
 * `addingParentId` 為 null 時代表新增頂層分類；有值時代表在該節點下新增子分類。
 * 深度上限對應 CATEGORY_MAX_DEPTH（後端 CategoryDepthExceededException 的前端提示）。
 */
const addingParentId = ref<string | null | undefined>(undefined)
const newCategoryName = ref('')

function startAddCategory(parentId: string | null) {
  addingParentId.value = parentId
  newCategoryName.value = ''
}

function confirmAddCategory() {
  const name = newCategoryName.value.trim()
  const parentId = addingParentId.value
  addingParentId.value = undefined
  if (!name) return

  if (parentId === null) {
    mockCategories.push({ id: `cat-${Date.now()}`, label: name })
    return
  }
  const parentDepth = depthOfCategory(mockCategories, parentId!) ?? 1
  if (parentDepth >= CATEGORY_MAX_DEPTH) {
    ElMessage.warning(`分類階層最多 ${CATEGORY_MAX_DEPTH} 層`)
    return
  }
  const parent = findCategoryNode(mockCategories, parentId!)
  if (parent) {
    parent.children = parent.children ?? []
    parent.children.push({ id: `cat-${Date.now()}`, label: name })
  }
}
</script>

<template>
  <!-- 參考 Eagle 圖片管理工具的左側深色導覽列：頂部固定導覽項目、中段分類樹狀、底部使用者資訊 -->
  <aside class="app-sidebar">
    <div class="app-sidebar__brand" @click="router.push('/')">
      <el-icon :size="20"><Files /></el-icon>
      <span>個人圖書館</span>
    </div>

    
    <div class="app-sidebar__section-title">藏書</div>
    <ul class="app-sidebar__quick-list">
      <li
        class="app-sidebar__quick-item"
        :class="{ 'app-sidebar__quick-item--active': activeQuickFilter === 'all' }"
        @click="goToQuickFilter('all')"
      >
        <span class="app-sidebar__quick-label"><el-icon><Notebook /></el-icon><span>全部</span></span>
        <span class="app-sidebar__count">{{ totalCount }}</span>
      </li>
      <li
        class="app-sidebar__quick-item"
        :class="{ 'app-sidebar__quick-item--active': activeQuickFilter === 'uncategorized' }"
        @click="goToQuickFilter('uncategorized')"
      >
        <span class="app-sidebar__quick-label"><el-icon><FolderRemove /></el-icon><span>未分類</span></span>
        <span class="app-sidebar__count">{{ uncategorizedCount }}</span>
      </li>
      <li
        class="app-sidebar__quick-item"
        :class="{ 'app-sidebar__quick-item--active': activeQuickFilter === 'untagged' }"
        @click="goToQuickFilter('untagged')"
      >
        <span class="app-sidebar__quick-label"><el-icon><Remove /></el-icon><span>未標籤</span></span>
        <span class="app-sidebar__count">{{ untaggedCount }}</span>
      </li>
      <li
        class="app-sidebar__quick-item"
        :class="{ 'app-sidebar__quick-item--active': route.path === '/tags' }"
        @click="router.push('/tags')"
      >
        <span class="app-sidebar__quick-label"><el-icon><PriceTag /></el-icon><span>標籤管理</span></span>
      </li>
      <li
        class="app-sidebar__quick-item"
        :class="{ 'app-sidebar__quick-item--active': route.path === '/categories' }"
        @click="router.push('/categories')"
      >
        <span class="app-sidebar__quick-label"><el-icon><FolderOpened /></el-icon><span>分類管理</span></span>
      </li>
    </ul>

    <div class="app-sidebar__section-title">
      <span>分類</span>
      <el-icon class="app-sidebar__add-icon" @click="startAddCategory(null)"><Plus /></el-icon>
    </div>
    <el-scrollbar class="app-sidebar__categories">
      <div v-if="addingParentId === null" class="app-sidebar__add-row">
        <el-input
          v-model="newCategoryName"
          size="small"
          placeholder="新分類名稱"
          autofocus
          @keyup.enter="confirmAddCategory"
          @blur="confirmAddCategory"
        />
      </div>
      <el-tree
        :data="mockCategories"
        node-key="id"
        :props="{ label: 'label', children: 'children' }"
        default-expand-all
        class="app-sidebar__tree"
      >
        <template #default="{ node, data }">
          <span class="app-sidebar__tree-node">
            <span class="app-sidebar__quick-label" @click="goToCategory(node.label)">
              <el-icon :style="{ color: data.color || '#9a9ca6' }"><FolderOpened /></el-icon>
              <span>{{ node.label }}</span>
            </span>
            <span class="app-sidebar__tree-actions">
              <span class="app-sidebar__count">{{ countByCategory(data) }}</span>
              <el-icon class="app-sidebar__add-icon" @click.stop="startAddCategory(data.id)"><Plus /></el-icon>
            </span>
          </span>
          <div v-if="addingParentId === data.id" class="app-sidebar__add-row app-sidebar__add-row--child">
            <el-input
              v-model="newCategoryName"
              size="small"
              placeholder="新子分類名稱"
              autofocus
              @keyup.enter="confirmAddCategory"
              @blur="confirmAddCategory"
            />
          </div>
        </template>
      </el-tree>
    </el-scrollbar>

    <div class="app-sidebar__footer">
      <UserInfoWidget />
    </div>
  </aside>
</template>

<style scoped>
.app-sidebar {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 232px;
  background-color: #1f2024;
  color: #c7c9d1;
  flex-shrink: 0;
}

.app-sidebar__brand {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  cursor: pointer;
  border-radius: 6px;
}

.app-sidebar__brand:hover {
  background-color: #2b2d33;
}

.app-sidebar__menu {
  border-right: none;
}

.app-sidebar__section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px 4px;
  font-size: 12px;
  color: #7c7e88;
  letter-spacing: 1px;
}

.app-sidebar__add-icon {
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
}

.app-sidebar__add-icon:hover {
  background-color: #2b2d33;
  color: #ffffff;
}

.app-sidebar__add-row {
  padding: 4px 8px 8px;
}

.app-sidebar__add-row--child {
  padding-left: 20px;
}

.app-sidebar__quick-list {
  list-style: none;
  margin: 0;
  padding: 0 8px;
}

.app-sidebar__quick-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.app-sidebar__quick-item:hover {
  background-color: #2b2d33;
}

.app-sidebar__quick-item--active {
  background-color: var(--el-color-primary);
  color: #ffffff;
}

.app-sidebar__quick-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-sidebar__count {
  font-size: 12px;
  color: #9a9ca6;
  min-width: 16px;
  text-align: right;
}

.app-sidebar__quick-item--active .app-sidebar__count {
  color: #ffffff;
}

.app-sidebar__categories {
  flex: 1;
  min-height: 0;
  padding: 0 8px;
}

.app-sidebar__tree {
  background-color: transparent;
  --el-tree-node-hover-bg-color: #2b2d33;
  --el-tree-text-color: #c7c9d1;
}

.app-sidebar__tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  gap: 6px;
  padding-right: 4px;
}

.app-sidebar__tree-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.app-sidebar__footer {
  border-top: 1px solid #2b2d33;
  padding: 8px 12px;
}
</style>

