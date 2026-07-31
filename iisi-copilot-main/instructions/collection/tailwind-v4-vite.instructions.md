---
description: '使用官方 @tailwindcss/vite 外掛程式為 Vite 專案安裝和設定 Tailwind CSS v4+'
applyTo: 'vite.config.ts, vite.config.js, **/*.css, **/*.tsx, **/*.ts, **/*.jsx, **/*.js'
---

# Tailwind CSS v4+ 安裝與 Vite 配置

使用官方 Vite 外掛程式安裝和配置 Tailwind CSS 版本 4 及以上。Tailwind CSS v4 引入了簡化的設置，消除了大多數情況下對 PostCSS 配置和 tailwind.config.js 的需求。

## Tailwind CSS v4 的主要變更

- **使用 Vite 外掛程式時不需要 PostCSS 配置**
- **不需要 tailwind.config.js** - 配置通過 CSS 完成
- **新的 @tailwindcss/vite 外掛程式** 取代了基於 PostCSS 的方法
- **CSS 優先配置** 使用 `@theme` 指令
- **自動內容檢測** - 無需指定內容路徑

## 安裝步驟

### 步驟 1：安裝依賴

安裝 `tailwindcss` 和 `@tailwindcss/vite` 外掛程式：

```bash
npm install tailwindcss @tailwindcss/vite
```

### 步驟 2：配置 Vite 外掛程式

將 `@tailwindcss/vite` 外掛程式添加到您的 Vite 配置文件中：

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [
    tailwindcss(),
  ],
})
```

對於使用 Vite 的 React 專案：

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
})
```

### 步驟 3：導入 Tailwind CSS

將 Tailwind CSS 導入到您的主 CSS 文件中（例如，`src/index.css` 或 `src/App.css`）：

```css
@import "tailwindcss";
```

### 步驟 4：在入口點驗證 CSS 導入

確保您的主 CSS 文件已在應用程序入口點中導入：

```typescript
// src/main.tsx 或 src/main.ts
import './index.css'
```

### 步驟 5：啟動開發伺服器

運行開發伺服器以驗證安裝：
```bash
npm run dev
```

## Tailwind v4 中不應該做的事情

### 不要創建 tailwind.config.js

Tailwind v4 使用 CSS 優先配置。除非有特定的舊版需求，否則不要創建 `tailwind.config.js` 文件。

```javascript
// ❌ NOT NEEDED in Tailwind v4
module.exports = {
  content: ['./src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {},
  },
  plugins: [],
}
```

### 不要為 Tailwind 建立 postcss.config.js 文件

當使用 `@tailwindcss/vite` 外掛程式時，不需要為 Tailwind 配置 PostCSS。

```javascript
// ❌ NOT NEEDED when using @tailwindcss/vite
module.exports = {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
```

### 不要使用舊的指令

舊的 `@tailwind` 指令已被單一導入取代：

```css
/* ❌ 舊的 - 在 Tailwind v4 中不要使用 */
@tailwind base;
@tailwind components;
@tailwind utilities;

/* ✅ 新的 - 在 Tailwind v4 中使用這個 */

@import "tailwindcss";
```

## CSS 優先配置（Tailwind v4）

### 自訂主題配置

使用 CSS 中的 `@theme` 指令來自訂設計標記：
```css
@import "tailwindcss";

@theme {
  --color-primary: #3b82f6;
  --color-secondary: #64748b;
  --font-sans: 'Inter', system-ui, sans-serif;
  --radius-lg: 0.75rem;
}
```

### 自訂工具類別

直接在 CSS 中定義自訂工具類別：

```css
@import "tailwindcss";

@utility content-auto {
  content-visibility: auto;
}

@utility scrollbar-hidden {
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
}
```

### 自訂變體

直接在 CSS 中定義自訂變體：

```css
@import "tailwindcss";

@variant hocus (&:hover, &:focus);
@variant group-hocus (:merge(.group):hover &, :merge(.group):focus &);
```

## 驗證清單

安裝完成後，請確認：

- [ ] `tailwindcss` 和 `@tailwindcss/vite` 已在 `package.json` 的依賴中
- [ ] `vite.config.ts` 包含 `tailwindcss()` 外掛程式
- [ ] 主 CSS 文件包含 `@import "tailwindcss";`
- [ ] CSS 文件已在應用程序入口點中導入
- [ ] 開發伺服器運行無錯誤
- [ ] Tailwind 工具類別（例如 `text-blue-500`、`p-4`）正確渲染

## 範例用法

使用簡單的組件測試安裝：

```tsx
export function TestComponent() {
  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <h1 className="text-3xl font-bold text-blue-600 underline">
        Hello, Tailwind CSS v4!
      </h1>
    </div>
  )
}
```

## 疑難排解

### 樣式未應用

1. 確認 CSS 導入語句為 `@import "tailwindcss";`（而不是舊的指令）
2. 確保 CSS 文件已在入口點中導入
3. 檢查 Vite 配置是否包含 `tailwindcss()` 外掛程式
4. 清除 Vite 緩存：`rm -rf node_modules/.vite && npm run dev`

### 外掛程式未找到錯誤

如果看到 "Cannot find module '@tailwindcss/vite'"：

```bash
npm install @tailwindcss/vite
```

### TypeScript 錯誤

如果 TypeScript 無法找到 Vite 外掛程式的類型，請確保您有正確的導入：

```typescript
import tailwindcss from '@tailwindcss/vite'
```

## 從 Tailwind v3 遷移

如果從 Tailwind v3 遷移：
1. 移除 tailwind.config.js （將自訂設定移至 CSS @theme ）
2. 移除 `postcss.config.js`（如果僅用於 Tailwind）
3. 卸載舊的套件：`npm uninstall postcss autoprefixer`
4. 安裝新套件：`npm install tailwindcss @tailwindcss/vite`
5. 將 `@tailwind` 指令替換為 `@import "tailwindcss";`
6. 更新 Vite 配置以使用 `@tailwindcss/vite` 外掛程式

## 參考

- 官方文件: https://tailwindcss.com/docs/installation/using-vite
- Tailwind CSS v4 升級指南: https://tailwindcss.com/docs/upgrade-guide
