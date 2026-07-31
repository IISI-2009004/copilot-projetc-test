<script setup lang="ts">
import { ref, computed } from 'vue'
import { PriceTag, Plus, Delete, Search, Close } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { mockBooks } from '@/mocks/mockData'
import { mockTags } from '../store'

const searchKeyword = ref('')

const filteredTags = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return mockTags
  return mockTags.filter((tag) => tag.name.toLowerCase().includes(keyword))
})

function bookCountOf(name: string): number {
  return mockBooks.filter((book) => book.tags.includes(name)).length
}

/** 隨機決定每個標籤方塊的大小（不規則排列，靈感來自 Eagle 標籤雲）。 */
const sizeOf = (id: string): 'sm' | 'md' | 'lg' => {
  const hash = Array.from(id).reduce((sum, ch) => sum + ch.charCodeAt(0), 0)
  const bucket = hash % 3
  return bucket === 0 ? 'lg' : bucket === 1 ? 'md' : 'sm'
}

/* 新增標籤 */
const isAdding = ref(false)
const newTagName = ref('')
const newTagColor = ref('#409eff')

function startAdd() {
  isAdding.value = true
  newTagName.value = ''
  newTagColor.value = '#409eff'
}

function confirmAdd() {
  const name = newTagName.value.trim()
  if (name && !mockTags.some((t) => t.name === name)) {
    mockTags.push({ id: `tag-${Date.now()}`, name, color: newTagColor.value })
  }
  isAdding.value = false
}

/* 選取標籤後才顯示編輯面板（改名／改色／刪除） */
const selectedId = ref<string | null>(null)
const editingName = ref('')

const selectedTag = computed(() => mockTags.find((t) => t.id === selectedId.value) ?? null)

function selectTag(id: string) {
  selectedId.value = id
  const tag = mockTags.find((t) => t.id === id)
  editingName.value = tag?.name ?? ''
}

function closePanel() {
  selectedId.value = null
}

function saveName() {
  const tag = selectedTag.value
  const name = editingName.value.trim()
  if (tag && name) tag.name = name
}

async function removeTag(id: string) {
  const tag = mockTags.find((t) => t.id === id)
  if (!tag) return

  const bookCount = bookCountOf(tag.name)
  if (bookCount > 0) {
    try {
      await ElMessageBox.confirm(
        `${bookCount} 本書籍已使用「${tag.name}」標籤，刪除後將自動移除這些書籍上的此標籤，確定要刪除嗎？`,
        '確認刪除標籤',
        { type: 'warning', confirmButtonText: '刪除', cancelButtonText: '取消' },
      )
    } catch {
      return
    }
    mockBooks.forEach((book) => {
      const idx = book.tags.indexOf(tag.name)
      if (idx !== -1) book.tags.splice(idx, 1)
    })
  }

  const index = mockTags.findIndex((t) => t.id === id)
  if (index !== -1) mockTags.splice(index, 1)
  if (selectedId.value === id) selectedId.value = null
}
</script>

<template>
  <div class="tag-management">
    <h2 class="tag-management__title"><el-icon><PriceTag /></el-icon> 標籤管理</h2>
    <p class="tag-management__hint">標籤可自由新增並自訂顯示顏色，點選標籤即可編輯（對應後端獨立 Tag 資料表）。</p>

    <div class="tag-management__toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜尋標籤..."
        clearable
        :prefix-icon="Search"
        class="tag-management__search"
      />
      <template v-if="!isAdding">
        <el-button type="primary" :icon="Plus" @click="startAdd">新增標籤</el-button>
      </template>
      <template v-else>
        <el-color-picker v-model="newTagColor" size="default" />
        <el-input v-model="newTagName" placeholder="新標籤名稱" style="width: 160px" @keyup.enter="confirmAdd" />
        <el-button type="primary" @click="confirmAdd">確定</el-button>
        <el-button @click="isAdding = false">取消</el-button>
      </template>
    </div>

    <div class="tag-management__cloud">
      <span
        v-for="tag in filteredTags"
        :key="tag.id"
        class="tag-management__chip"
        :class="[`tag-management__chip--${sizeOf(tag.id)}`, { 'tag-management__chip--active': selectedId === tag.id }]"
        :style="tag.color ? { backgroundColor: tag.color, color: '#ffffff', borderColor: tag.color } : undefined"
        @click="selectTag(tag.id)"
      >
        {{ tag.name }}
        <span class="tag-management__chip-count">{{ bookCountOf(tag.name) }}</span>
      </span>
      <p v-if="filteredTags.length === 0" class="tag-management__empty">找不到符合的標籤。</p>
    </div>

    <!-- 點選標籤後彈出的編輯面板 -->
    <el-dialog :model-value="!!selectedTag" title="編輯標籤" width="360px" @update:model-value="closePanel" @close="closePanel">
      <div v-if="selectedTag" class="tag-management__edit">
        <div class="tag-management__edit-row">
          <span class="tag-management__edit-label">名稱</span>
          <el-input v-model="editingName" @blur="saveName" @keyup.enter="saveName" />
        </div>
        <div class="tag-management__edit-row">
          <span class="tag-management__edit-label">顏色</span>
          <el-color-picker v-model="selectedTag.color" />
        </div>
        <div class="tag-management__edit-row">
          <span class="tag-management__edit-label">使用中</span>
          <span>{{ bookCountOf(selectedTag.name) }} 本書籍</span>
        </div>
      </div>
      <template #footer>
        <el-button :icon="Delete" type="danger" plain @click="removeTag(selectedTag!.id)">刪除標籤</el-button>
        <el-button :icon="Close" @click="closePanel">關閉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.tag-management__title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-management__hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin: 0 0 20px;
}

.tag-management__toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tag-management__search {
  width: 220px;
}

.tag-management__cloud {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 14px;
  max-width: 720px;
}

.tag-management__chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  background-color: #f0f1f5;
  border: 1px solid #e4e6ec;
  color: #4c4e58;
  font-size: 13px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.tag-management__chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.tag-management__chip--active {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}

.tag-management__chip--sm {
  font-size: 12px;
  padding: 4px 10px;
}

.tag-management__chip--md {
  font-size: 14px;
  padding: 7px 16px;
}

.tag-management__chip--lg {
  font-size: 17px;
  padding: 10px 20px;
  font-weight: 600;
}

.tag-management__chip-count {
  font-size: 11px;
  opacity: 0.7;
}

.tag-management__empty {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.tag-management__edit {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tag-management__edit-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tag-management__edit-label {
  width: 56px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
