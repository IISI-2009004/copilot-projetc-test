<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createBook } from '@/api/books'
import { ApiError } from '@/utils/http'
import {
  BOOK_TYPE_LABEL,
  PHYSICAL_OR_EBOOK_TYPES,
  ONLINE_BOOK_TYPES,
  type BookCreateRequest,
  type BookResponse,
  type BookType,
} from '@/types/bookApi'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  created: [book: BookResponse]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

const bookTypeOptions = Object.entries(BOOK_TYPE_LABEL).map(([value, label]) => ({
  value: value as BookType,
  label,
}))

function isPhysicalOrEbook(bookType: BookType | ''): boolean {
  return PHYSICAL_OR_EBOOK_TYPES.includes(bookType as BookType)
}

function isOnline(bookType: BookType | ''): boolean {
  return ONLINE_BOOK_TYPES.includes(bookType as BookType)
}

const formRef = ref<FormInstance>()
const submitting = ref(false)

const defaultForm = () => ({
  title: '',
  author: '',
  bookType: '' as BookType | '',
  isbn: '',
  url: '',
  sourcePlatform: '',
  purchaseUrl: '',
  authorUrl: '',
})

const form = reactive(defaultForm())

const URL_PATTERN = /^https?:\/\/.+/

const rules = computed<FormRules>(() => ({
  title: [{ required: true, message: '請輸入書名／作品名', trigger: 'blur' }],
  author: [{ required: true, message: '請輸入作者', trigger: 'blur' }],
  bookType: [{ required: true, message: '請選擇書本類型', trigger: 'change' }],
  isbn: [
    {
      pattern: /^\d{10}$|^\d{13}$/,
      message: 'ISBN 須為 10 或 13 位數字',
      trigger: 'blur',
    },
  ],
  url: [
    {
      required: isOnline(form.bookType),
      message: '線上內容類必須填寫來源網址',
      trigger: 'blur',
    },
    { pattern: URL_PATTERN, message: 'url 須為 http/https 網址', trigger: 'blur' },
  ],
  purchaseUrl: [{ pattern: URL_PATTERN, message: '須為 http/https 網址', trigger: 'blur' }],
  authorUrl: [{ pattern: URL_PATTERN, message: '須為 http/https 網址', trigger: 'blur' }],
}))

function resetForm() {
  Object.assign(form, defaultForm())
  formRef.value?.clearValidate()
}

function close() {
  visible.value = false
  resetForm()
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const bookType = form.bookType as BookType
  const request: BookCreateRequest = {
    title: form.title.trim(),
    author: form.author.trim(),
    bookType,
    isbn: isPhysicalOrEbook(bookType) && form.isbn ? form.isbn.trim() : null,
    url: isOnline(bookType) && form.url ? form.url.trim() : null,
    sourcePlatform: isOnline(bookType) && form.sourcePlatform ? form.sourcePlatform.trim() : null,
    purchaseUrl: form.purchaseUrl ? form.purchaseUrl.trim() : null,
    authorUrl: form.authorUrl ? form.authorUrl.trim() : null,
  }

  submitting.value = true
  try {
    const created = await createBook(request)
    ElMessage.success('已新增書本')
    emit('created', created)
    close()
  } catch (error) {
    const message = error instanceof ApiError ? error.message : '新增書本失敗，請稍後再試'
    ElMessage.error(message)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog v-model="visible" title="新增書本" width="480px" @close="resetForm">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px" label-position="right">
      <el-form-item label="書本類型" prop="bookType">
        <el-select v-model="form.bookType" placeholder="請選擇書本類型" style="width: 100%">
          <el-option v-for="option in bookTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="書名" prop="title">
        <el-input v-model="form.title" placeholder="請輸入書名／作品名" />
      </el-form-item>
      <el-form-item label="作者" prop="author">
        <el-input v-model="form.author" placeholder="請輸入作者" />
      </el-form-item>

      <template v-if="isPhysicalOrEbook(form.bookType)">
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="10 或 13 位數字（選填）" />
        </el-form-item>
      </template>

      <template v-if="isOnline(form.bookType)">
        <el-form-item label="來源網址" prop="url">
          <el-input v-model="form.url" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="來源平台" prop="sourcePlatform">
          <el-input v-model="form.sourcePlatform" placeholder="例如 AO3、Wattpad（選填）" />
        </el-form-item>
      </template>

      <el-form-item label="購買網址" prop="purchaseUrl">
        <el-input v-model="form.purchaseUrl" placeholder="https://...（選填）" />
      </el-form-item>
      <el-form-item label="作者網址" prop="authorUrl">
        <el-input v-model="form.authorUrl" placeholder="https://...（選填）" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">新增</el-button>
    </template>
  </el-dialog>
</template>
