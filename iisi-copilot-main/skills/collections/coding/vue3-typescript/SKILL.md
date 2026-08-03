---
name: "vue3-typescript"
description: "開發或維護使用 Vue 3 Composition API 與 TypeScript 的前端元件與頁面"
skill: vue3-typescript
version: 1.0
tags: [vue3, typescript, frontend]
applicable-to: [Vue 3, TypeScript]
last-updated: 2026-07-20
---

# Vue3 + TypeScript 技能

## 適用情境

開發或維護使用 Vue 3 Composition API 與 TypeScript 的前端元件與頁面。

## 核心原則

1. 一律使用 `<script setup lang="ts">`，不使用 Options API
2. 元件 Props / Emits 必須有明確型別定義
3. 共用邏輯抽為 Composable（`useXxx`），不得複製貼上
4. 狀態管理使用 Pinia，禁止跨元件直接存取彼此的內部 ref

## 標準做法

### 型別化的元件

```vue
<script setup lang="ts">
interface Props {
  accountId: string;
  readonly?: boolean;
}
const props = withDefaults(defineProps<Props>(), { readonly: false });

const emit = defineEmits<{
  (e: "update", balance: number): void;
}>();
</script>
```

### Composable 抽取共用邏輯

```typescript
export function useAccountBalance(accountId: Ref<string>) {
  const balance = ref<number | null>(null);
  const loading = ref(false);

  async function refresh() {
    loading.value = true;
    balance.value = await accountApi.getBalance(accountId.value);
    loading.value = false;
  }

  watch(accountId, refresh, { immediate: true });
  return { balance, loading, refresh };
}
```

## 禁止事項

- 不得使用 `any` 型別逃避型別檢查
- 不得在元件內直接 `fetch`，一律透過封裝的 API Client
- 不得使用 Options API 混寫在同一專案中造成風格不一致

## 驗收標準

- [ ] `vue-tsc --noEmit` 無型別錯誤
- [ ] Props / Emits 皆有型別，無隱式 `any`
- [ ] 重複超過兩次的邏輯已抽為 Composable

## 參考資料

- Vue 3 官方文件－Composition API FAQ
- Vue 3 官方文件－TypeScript 支援指南
