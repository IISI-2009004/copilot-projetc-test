<script setup lang="ts">
import { computed, ref, toRef } from 'vue'
import { Close, Calendar, PriceTag, FolderOpened, Plus, Camera } from '@element-plus/icons-vue'
import type { Book } from '@/types/book'
import { BOOK_STATUS_LABEL } from '@/types/book'
import { useBookCoverGradient } from '@/composables/useBookCoverGradient'
import { mockCategories, mockTags, useTagColor, flattenCategories } from '@/modules/taxonomy'

const props = defineProps<{ book: Book }>()
defineEmits<{ close: [] }>()

const gradient = useBookCoverGradient(toRef(props, 'book'))

/** 上傳封面圖片跳窗開關與檔案輸入元素參照。 */
const uploadDialogVisible = ref(false)
const fileInput = ref<HTMLInputElement>()
const uploadPreview = ref<string>()

function openUploadDialog() {
  uploadPreview.value = props.book.coverUrl
  uploadDialogVisible.value = true
}

function triggerFileSelect() {
  fileInput.value?.click()
}

function onFileSelected(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => {
    uploadPreview.value = reader.result as string
  }
  reader.readAsDataURL(file)
}

function confirmUpload() {
  props.book.coverUrl = uploadPreview.value
  uploadDialogVisible.value = false
}

/** 分類下拉選項：攤平 mockCategories 樹狀結構，並依深度加上縮排前綴以呈現階層關係。 */
const categoryOptions = computed(() =>
  flattenCategories(mockCategories).map(({ node, depth }) => ({
    label: `${'　'.repeat(depth - 1)}${depth > 1 ? '└ ' : ''}${node.label}`,
    value: node.label,
  })),
)

/** 使用者於下拉輸入新分類名稱時，於頂層新增分類節點（如需歸入子分類，請至側邊欄分類樹操作）。 */
function onCreateCategory(value: string) {
  const trimmed = value.trim()
  if (trimmed && !categoryOptions.value.some((opt) => opt.value === trimmed)) {
    mockCategories.push({ id: `cat-${Date.now()}`, label: trimmed })
  }
}

const statusOptions = Object.entries(BOOK_STATUS_LABEL).map(([value, label]) => ({ value, label }))

const newTag = ref('')
const { tagColor } = useTagColor()

/** 新增標籤 input 的模糊查詢建議：從既有 mockTags 中比對輸入內容，排除已加在此書上的標籤。 */
function queryTagSuggestions(queryString: string, callback: (results: { value: string }[]) => void) {
  const keyword = queryString.trim().toLowerCase()
  const results = mockTags
    .filter((tag) => !props.book.tags.includes(tag.name))
    .filter((tag) => !keyword || tag.name.toLowerCase().includes(keyword))
    .map((tag) => ({ value: tag.name }))
  callback(results)
}

function addTag(value?: string) {
  const raw = value ?? newTag.value
  const trimmed = raw.trim()
  if (trimmed && !props.book.tags.includes(trimmed)) {
    props.book.tags.push(trimmed)
    if (!mockTags.some((t) => t.name === trimmed)) {
      mockTags.push({ id: `tag-${Date.now()}`, name: trimmed })
    }
  }
  newTag.value = ''
}

function onTagSelect(item: { value: string }) {
  addTag(item.value)
}

function removeTag(tag: string) {
  const index = props.book.tags.indexOf(tag)
  if (index !== -1) props.book.tags.splice(index, 1)
}

function toDateInput(iso?: string) {
  if (!iso) return ''
  return iso.slice(0, 10)
}

function onDateInput(event: Event) {
  const value = (event.target as HTMLInputElement).value
  props.book.addedAt = value ? new Date(value).toISOString() : undefined
}
</script>

<template>
  <!-- 參考 Eagle 點選項目後於右側顯示的「詳細資訊面板」（Inspector），Scaffold 階段可直接編輯並即時反映到列表。 -->
  <aside class="book-detail-panel">
    <div class="book-detail-panel__header">
      <span class="book-detail-panel__header-title">詳細資訊</span>
      <el-button link :icon="Close" @click="$emit('close')" />
    </div>

    <div
      class="book-detail-panel__cover"
      :style="{ background: book.coverUrl ? undefined : gradient }"
      @click="openUploadDialog"
    >
      <img v-if="book.coverUrl" :src="book.coverUrl" class="book-detail-panel__cover-img" alt="" />
      <span v-else class="book-detail-panel__initial">{{ book.title.charAt(0) }}</span>
      <div class="book-detail-panel__cover-overlay">
        <el-icon><Camera /></el-icon>
        <span>更換封面</span>
      </div>
    </div>

    <div class="book-detail-panel__body">
      <el-input v-model="book.title" class="book-detail-panel__title-input" placeholder="書名" />
      <el-input v-model="book.author" class="book-detail-panel__author-input" placeholder="作者" />

      <el-select v-model="book.status" size="small" class="book-detail-panel__status">
        <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>

      <div class="book-detail-panel__field">
        <label><el-icon><FolderOpened /></el-icon> 分類</label>
        <el-select
          v-model="book.category"
          size="small"
          clearable
          filterable
          allow-create
          default-first-option
          placeholder="未分類"
          style="width: 100%"
          @create="onCreateCategory"
        >
          <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </div>

      <div class="book-detail-panel__field">
        <label>ISBN</label>
        <el-input v-model="book.isbn" size="small" placeholder="尚未填寫" />
      </div>

      <div class="book-detail-panel__field">
        <label><el-icon><Calendar /></el-icon> 加入日期</label>
        <input
          type="date"
          class="book-detail-panel__date-input"
          :value="toDateInput(book.addedAt)"
          @change="onDateInput"
        />
      </div>

      <div class="book-detail-panel__section">
        <div class="book-detail-panel__section-title"><el-icon><PriceTag /></el-icon> 標籤</div>
        <div class="book-detail-panel__tags">
          <el-tag
            v-for="tag in book.tags"
            :key="tag"
            size="small"
            round
            closable
            :color="tagColor(tag)"
            :style="tagColor(tag) ? { color: '#ffffff', border: 'none' } : undefined"
            @close="removeTag(tag)"
          >
            {{ tag }}
          </el-tag>
        </div>
        <div class="book-detail-panel__tag-input">
          <el-autocomplete
            v-model="newTag"
            size="small"
            placeholder="新增標籤"
            :fetch-suggestions="queryTagSuggestions"
            style="flex: 1"
            @select="onTagSelect"
            @keyup.enter="addTag()"
          />
          <el-button size="small" :icon="Plus" @click="addTag()" />
        </div>
      </div>

      <div class="book-detail-panel__section">
        <div class="book-detail-panel__section-title">簡介</div>
        <el-input
          v-model="book.description"
          type="textarea"
          :rows="4"
          placeholder="尚未填寫簡介"
        />
      </div>
    </div>
  </aside>

  <el-dialog v-model="uploadDialogVisible" title="上傳封面圖片" width="360px" append-to-body>
    <div class="cover-upload">
      <div class="cover-upload__preview">
        <img v-if="uploadPreview" :src="uploadPreview" alt="封面預覽" />
        <div v-else class="cover-upload__placeholder" :style="{ background: gradient }">
          <span>{{ book.title.charAt(0) }}</span>
        </div>
      </div>
      <input ref="fileInput" type="file" accept="image/*" hidden @change="onFileSelected" />
      <el-button class="cover-upload__select" @click="triggerFileSelect">選擇圖片</el-button>
    </div>
    <template #footer>
      <el-button @click="uploadDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmUpload">確定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.book-detail-panel {
  width: 280px;
  flex-shrink: 0;
  background-color: #ffffff;
  border-left: 1px solid var(--el-border-color);
  height: 100vh;
  overflow-y: auto;
}

.book-detail-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.book-detail-panel__header-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
}

.book-detail-panel__cover {
  position: relative;
  width: 120px;
  aspect-ratio: 3 / 4;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 16px auto;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.book-detail-panel__cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-detail-panel__cover-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background-color: rgba(0, 0, 0, 0.55);
  color: #ffffff;
  font-size: 11px;
  opacity: 0;
  transition: opacity 0.15s ease;
}

.book-detail-panel__cover:hover .book-detail-panel__cover-overlay {
  opacity: 1;
}

.book-detail-panel__initial {
  font-size: 32px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.85);
}

.book-detail-panel__body {
  padding: 0 16px 20px;
}

.book-detail-panel__title-input :deep(.el-input__inner) {
  font-size: 16px;
  font-weight: 600;
}

.book-detail-panel__title-input {
  margin-bottom: 6px;
}

.book-detail-panel__author-input {
  margin-bottom: 10px;
}

.book-detail-panel__status {
  margin-bottom: 16px;
  width: 100%;
}

.book-detail-panel__field {
  margin-bottom: 14px;
}

.book-detail-panel__field label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.book-detail-panel__date-input {
  width: 100%;
  box-sizing: border-box;
  padding: 4px 8px;
  font-size: 13px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  color: var(--el-text-color-regular);
}

.book-detail-panel__section {
  margin-bottom: 16px;
}

.book-detail-panel__section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.book-detail-panel__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.book-detail-panel__tag-input {
  display: flex;
  gap: 6px;
}

.book-detail-panel__tag-input .el-input {
  flex: 1;
}

.cover-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.cover-upload__preview {
  width: 160px;
  aspect-ratio: 3 / 4;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--el-border-color);
}

.cover-upload__preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-upload__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.85);
  font-size: 40px;
  font-weight: 700;
}
</style>
