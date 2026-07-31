<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { FolderOpened, Plus, Delete, Search, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElTree } from 'element-plus'
import { mockBooks } from '@/mocks/mockData'
import { mockCategories } from '../store'
import { CATEGORY_MAX_DEPTH, type Category } from '../types'
import { depthOfCategory, findCategoryNode, findCategoryParent, countDescendants } from '../categoryTree'

/** 分類管理頁：對應後端 Category 資料表（Adjacency List 樹狀階層），比照 Menu 側邊欄的樹狀呈現。 */

const searchKeyword = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

watch(searchKeyword, (val) => {
  treeRef.value?.filter(val)
})

function filterNode(value: string, data: unknown) {
  if (!value) return true
  return (data as Category).label.toLowerCase().includes(value.toLowerCase())
}

function bookCountOf(node: Category): number {
  return mockBooks.filter((book) => book.category === node.label).length
}

/* 新增分類（頂層或指定父節點下的子分類） */
const addingParentId = ref<string | null | undefined>(undefined)
const newCategoryName = ref('')
const newCategoryColor = ref('#7c7e88')

function startAdd(parentId: string | null) {
  addingParentId.value = parentId
  newCategoryName.value = ''
  newCategoryColor.value = '#7c7e88'
}

function cancelAdd() {
  addingParentId.value = undefined
}

function confirmAdd() {
  const name = newCategoryName.value.trim()
  const parentId = addingParentId.value
  if (!name) return

  if (parentId === null) {
    mockCategories.push({ id: `cat-${Date.now()}`, label: name, color: newCategoryColor.value })
    addingParentId.value = undefined
    return
  }
  const parentDepth = depthOfCategory(mockCategories, parentId!) ?? 1
  if (parentDepth >= CATEGORY_MAX_DEPTH) {
    ElMessage.warning(`分類階層最多 ${CATEGORY_MAX_DEPTH} 層，無法在此節點下新增子分類`)
    return
  }
  const parent = findCategoryNode(mockCategories, parentId!)
  if (parent) {
    parent.children = parent.children ?? []
    parent.children.push({ id: `cat-${Date.now()}`, label: name, color: newCategoryColor.value })
  }
  addingParentId.value = undefined
}

/* 選取分類節點後才顯示編輯面板（改名／改色／刪除） */
const selectedId = ref<string | null>(null)
const editingName = ref('')

const selectedNode = computed(() => (selectedId.value ? findCategoryNode(mockCategories, selectedId.value) : null))

function selectCategory(node: Category) {
  selectedId.value = node.id
  editingName.value = node.label
}

function closePanel() {
  selectedId.value = null
}

function saveName() {
  const node = selectedNode.value
  const name = editingName.value.trim()
  if (node && name) node.label = name
}

async function removeCategory(node: Category) {
  const descendantCount = countDescendants(node)
  const bookCount = bookCountOf(node)
  const messages: string[] = []
  if (descendantCount > 0) messages.push(`包含 ${descendantCount} 個子分類`)
  if (bookCount > 0) messages.push(`${bookCount} 本書籍已使用此分類`)

  if (messages.length > 0) {
    try {
      await ElMessageBox.confirm(
        `此分類${messages.join('、')}，刪除後子分類將一併移除，書籍將變為「未分類」，確定要刪除嗎？`,
        '確認刪除分類',
        { type: 'warning', confirmButtonText: '刪除', cancelButtonText: '取消' },
      )
    } catch {
      return
    }
  }

  if (bookCount > 0) {
    mockBooks.forEach((book) => {
      if (book.category === node.label) book.category = undefined
    })
  }
  const parent = findCategoryParent(mockCategories, node.id)
  if (parent) {
    parent.children = (parent.children ?? []).filter((child) => child.id !== node.id)
  } else {
    const index = mockCategories.findIndex((c) => c.id === node.id)
    if (index !== -1) mockCategories.splice(index, 1)
  }
  closePanel()
}
</script>

<template>
  <div class="category-management">
    <h2 class="category-management__title"><el-icon><FolderOpened /></el-icon> 分類管理</h2>
    <p class="category-management__hint">
      分類保留樹狀階層（最多 {{ CATEGORY_MAX_DEPTH }} 層），比照 Menu 顯示；點選分類即可編輯名稱、顏色與刪除。
    </p>

    <div class="category-management__toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜尋分類..."
        clearable
        :prefix-icon="Search"
        class="category-management__search"
      />
      <template v-if="addingParentId === undefined">
        <el-button type="primary" :icon="Plus" @click="startAdd(null)">新增頂層分類</el-button>
      </template>
      <template v-else-if="addingParentId === null">
        <el-color-picker v-model="newCategoryColor" size="default" />
        <el-input v-model="newCategoryName" placeholder="新分類名稱" style="width: 160px" @keyup.enter="confirmAdd" />
        <el-button type="primary" @click="confirmAdd">確定</el-button>
        <el-button @click="cancelAdd">取消</el-button>
      </template>
    </div>

    <el-tree
      ref="treeRef"
      :data="mockCategories"
      node-key="id"
      :props="{ label: 'label', children: 'children' }"
      default-expand-all
      :filter-node-method="filterNode"
      class="category-management__tree"
    >
      <template #default="{ data }">
        <span class="category-management__node">
          <span class="category-management__node-main" @click="selectCategory(data)">
            <el-icon :style="{ color: data.color || '#9a9ca6' }"><FolderOpened /></el-icon>
            <span class="category-management__node-label">{{ data.label }}</span>
            <span class="category-management__count">{{ bookCountOf(data) }} 本</span>
          </span>
          <el-icon
            v-if="(depthOfCategory(mockCategories, data.id) ?? 1) < CATEGORY_MAX_DEPTH"
            class="category-management__add-icon"
            title="新增子分類"
            @click.stop="startAdd(data.id)"
          >
            <Plus />
          </el-icon>
        </span>
        <div v-if="addingParentId === data.id" class="category-management__add-row" @click.stop>
          <el-color-picker v-model="newCategoryColor" size="small" />
          <el-input
            v-model="newCategoryName"
            size="small"
            placeholder="新子分類名稱"
            autofocus
            @keyup.enter="confirmAdd"
          />
          <el-button size="small" type="primary" @click="confirmAdd">確定</el-button>
          <el-button size="small" @click="cancelAdd">取消</el-button>
        </div>
      </template>
    </el-tree>

    <!-- 點選分類節點後彈出的編輯面板 -->
    <el-dialog :model-value="!!selectedNode" title="編輯分類" width="360px" @update:model-value="closePanel" @close="closePanel">
      <div v-if="selectedNode" class="category-management__edit">
        <div class="category-management__edit-row">
          <span class="category-management__edit-label">名稱</span>
          <el-input v-model="editingName" @blur="saveName" @keyup.enter="saveName" />
        </div>
        <div class="category-management__edit-row">
          <span class="category-management__edit-label">顏色</span>
          <el-color-picker v-model="selectedNode.color" />
        </div>
        <div class="category-management__edit-row">
          <span class="category-management__edit-label">使用中</span>
          <span>{{ bookCountOf(selectedNode) }} 本書籍</span>
        </div>
      </div>
      <template #footer>
        <el-button :icon="Delete" type="danger" plain @click="removeCategory(selectedNode!)">刪除分類</el-button>
        <el-button :icon="Close" @click="closePanel">關閉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.category-management__title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-management__hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin: 0 0 20px;
  max-width: 560px;
}

.category-management__toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.category-management__search {
  width: 220px;
}

.category-management__tree {
  max-width: 480px;
  background-color: #ffffff;
  border-radius: 8px;
  padding: 8px 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.category-management__tree :deep(.el-tree-node__content) {
  height: 36px;
}

.category-management__node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  padding-right: 4px;
}

.category-management__node-main {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.category-management__node-label {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-management__count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.category-management__add-icon {
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  flex-shrink: 0;
}

.category-management__add-icon:hover {
  background-color: #f0f1f5;
}

.category-management__add-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 0 6px 24px;
}

.category-management__edit {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-management__edit-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-management__edit-label {
  width: 56px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
