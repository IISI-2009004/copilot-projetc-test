---
applyTo: '**'
description: '基於核心 Web 指標 (LCP、INP、CLS) 的綜合 Web 效能標準，包含 50 多個反模式、偵測正規表示式、針對現代 Web 框架的框架特定修復以及現代 API 指南。'
---

# Performance Standards 效能標準

全面的 Web 應用程式開發效能規則。每種反模式都包含嚴重性分類、檢測方法、受影響的核心 Web 指標以及修正程式碼範例。

**Severity levels:**

- **CRITICAL** — 直接使核心 Web 指標降至「差」的閾值以下。必須在合併前修復。
- **IMPORTANT** — 明顯影響使用者體驗。在同一個迭代中修復。
- **SUGGESTION** — 優化機會。計劃在未來的迭代中進行。

---

## Core Web Vitals Quick Reference 核心 Web 指標快速參考

### LCP (Largest Contentful Paint 最大內容繪製)

**Good: < 2.5s | Needs Improvement: 2.5-4s | Poor: > 4s**

衡量最大可見內容元素完成渲染的時間。四個連續階段：

| Phase | Target | What It Measures |
|-------|--------|-----------------|
| TTFB | ~40% of budget | 伺服器回應時間 |
| Resource Load Delay | < 10% | 從 TTFB 到 LCP 資源抓取開始的時間 |
| Resource Load Duration | ~40% | LCP 資源的下載時間 |
| Element Render Delay | < 10% | 從下載到繪製的時間 |

### INP (Interaction to Next Paint 用戶互動到下一次繪製)

**Good: < 200ms | Needs Improvement: 200-500ms | Poor: > 500ms**

測量所有用戶互動的延遲，並報告最差的延遲。分為三個階段：

| Phase | Optimization |
|-------|-------------|
| Input Delay | 拆分長任務，讓出給瀏覽器 |
| Processing Time | 保持處理程序 < 50ms |
| Presentation Delay | 最小化 DOM 大小，避免強制佈局 |

> **Diagnostic tool:** 使用 Long Animation Frames (LoAF) API (Chrome 123+) 來調試 INP 問題。LoAF 提供比舊的 Long Tasks API 更好的歸因，包括腳本來源和渲染時間。

### CLS (Cumulative Layout Shift 累積佈局偏移)

**Good: < 0.1 | Needs Improvement: 0.1-0.25 | Poor: > 0.25**

版面偏移來源包括：無尺寸標註的圖片、動態注入的內容、網頁字體 FOUT 以及延遲載入的廣告。使用者互動後 500 毫秒內的佈局偏移不受此影響。

---

## Loading and LCP Anti-Patterns (L1-L10) 載入和 LCP 反模式

### L1: Render-Blocking CSS Without Critical Extraction 無需關鍵提取即可渲染阻塞的 CSS

- **Severity**: CRITICAL
- **Detection**: `<link.*rel="stylesheet"` in `<head>` loading large CSS
- **CWV**: LCP

```html
<!-- BAD -->
<link rel="stylesheet" href="/styles/main.css" />

<!-- GOOD — inline關鍵 CSS（在建置時擷取），其餘部分預先載入 -->
<style>/* 關鍵的首屏 CSS，由 Critters/Beasties 等工具內聯。 */</style>
<link rel="preload" href="/styles/main.css" as="style" />
<link rel="stylesheet" href="/styles/main.css" />
```

優先選擇在建置時擷取關鍵 CSS（例如，使用 Critters、Beasties、Next.js experimental.optimizeCss ），並配合常規的 <link rel="stylesheet"> 。避免使用舊的 media="print" onload="this.media='all'" 技巧：在嚴格的 CSP（無 'unsafe-inline' / 無 script-src-attr 'unsafe-inline' ）下，內聯事件處理程序會被阻止，這將導致樣式表無法激活，從而造成樣式回歸。如果確實需要延遲載入非關鍵 CSS，請使用外部腳本切換 media ，而不是使用內聯處理程序。

### L2: Render-Blocking Synchronous Script 渲染阻塞同步腳本

- **Severity**: CRITICAL
- **Detection**: `<script.*src=` without `async|defer|type="module"`
- **CWV**: LCP

```html
<!-- BAD -->
<script src="/vendor/analytics.js"></script>

<!-- GOOD -->
<script src="/vendor/analytics.js" defer></script>
```

### L3: Missing Preconnect to Critical Origins 缺少對關鍵來源的預連接

- **Severity**: IMPORTANT
- **Detection**: Third-party API/CDN URLs without `<link rel="preconnect">`
- **CWV**: LCP

```html
<link rel="preconnect" href="https://api.example.com" />
<link rel="dns-prefetch" href="https://analytics.example.com" />
```

### L4: Missing Preload for LCP Resource 缺少對 LCP 資源的預加載

- **Severity**: CRITICAL
- **Detection**: LCP image/font not preloaded
- **CWV**: LCP

```html
<link rel="preload" as="image" href="/hero.webp" fetchpriority="high" />
```

### L5: Client-Side Data Fetching for Main Content 客戶端數據抓取主要內容

- **Severity**: CRITICAL
- **Detection**: `useEffect.*fetch|useEffect.*axios|ngOnInit.*subscribe`
- **CWV**: LCP

```tsx
// BAD — content appears after JS execution + API call
'use client';
function Page() {
  const [data, setData] = useState(null);
  useEffect(() => { fetch('/api/data').then(r => r.json()).then(setData); }, []);
  return <div>{data?.title}</div>;
}

// GOOD — Server Component fetches data before HTML is sent
async function Page() {
  const data = await fetch('https://api.example.com/data').then(r => r.json());
  return <div>{data.title}</div>;
}
```

### L6: Excessive Redirect Chains 過多的重定向鏈

- **Severity**: IMPORTANT
- **Detection**: Multiple sequential redirects (HTTP 301/302 chains)
- **CWV**: LCP

Each redirect adds 200-300ms. Maximum one redirect.

### L7: Missing fetchpriority on LCP Element 缺少對 LCP 元素的 fetchpriority
- **Detection**: Above-fold hero image without `fetchpriority="high"` or `priority` prop
- **CWV**: LCP

```tsx
// Next.js
<Image src="/hero.webp" alt="Hero" width={1200} height={600} priority />

// Angular
<img ngSrc="/hero.webp" alt="Hero" width="1200" height="600" priority>

// Plain HTML
<img src="/hero.webp" alt="Hero" width="1200" height="600" fetchpriority="high" />
```

### L8: Third-Party Scripts in Head Without Async/Defer 頭部的第三方腳本沒有 async/defer

- **Severity**: IMPORTANT
- **Detection**: `<script.*src="https://` without `async|defer`
- **CWV**: LCP

Defer non-essential scripts. Use facade pattern for chat widgets.

### L9: Oversized Initial HTML (>14KB) 初始 HTML 超過 14KB

- **Severity**: SUGGESTION
- **Detection**: Server-rendered HTML larger than 14KB
- **CWV**: LCP

Reduce inline CSS/JS, remove whitespace, use streaming SSR with Suspense boundaries.

### L10: Missing Compression at CDN/Server Level 缺少 CDN/服务器级别的压缩

- **Severity**: IMPORTANT
- **Detection**: Server not returning `content-encoding: br` or `gzip`
- **CWV**: LCP

Enable Brotli (15-25% better than gzip) at CDN/server level.

---

## Rendering and Hydration Anti-Patterns (R1-R8) 渲染和水合反模式

### R1: Entire Component Tree Marked "use client" 整個組件樹標記為 "use client"

- **Severity**: CRITICAL
- **Detection**: `"use client"` at top-level layout or page component
- **CWV**: LCP + INP

Push `"use client"` down to leaf components that need interactivity.

### R2: Missing Suspense Boundaries for Async Data 缺少異步數據的 Suspense 邊界

- **Severity**: IMPORTANT
- **Detection**: Server Components doing data fetching without `<Suspense>`
- **CWV**: LCP

```tsx
// GOOD — stream shell immediately, fill in data progressively
async function Page() {
  const user = await getUser();
  return (
    <div>
      <Header user={user} />
      <Suspense fallback={<PostsSkeleton />}>
        <Posts />
      </Suspense>
    </div>
  );
}
```

### R3: Hydration Mismatch from Dynamic Client Content 動態客戶端內容導致水合作用不匹配

- **Severity**: IMPORTANT
- **Detection**: `Date.now()|Math.random()|window\.innerWidth` in SSR components
- **CWV**: CLS

對於僅客戶端值，請使用 useEffect 已知差異，請使用 suppressHydrationWarning 。

### R4: Missing Streaming for Slow Data Sources 缺少對慢速數據源的流式處理

- **Severity**: IMPORTANT
- **Detection**: Page awaiting all data before sending HTML
- **CWV**: LCP (TTFB)

使用帶有 Suspense 邊界的流式 SSR。 Shell 會立即載入資料流；慢資料會逐步填入。

### R5: Unstable References Causing Re-renders  不穩定的引用導致重新渲染

- **Severity**: IMPORTANT
- **Detection**: `style=\{\{|onClick=\{\(\) =>` inline in JSX
- **CWV**: INP

React 19+ 版本，啟用 React 編譯器（需使用獨立的 Babel/SWC 建置外掛程式）：自動快取。未啟用編譯器：使用 useMemo / useCallback 擷取或快取。 Angular：OnPush。 Vue： computed() 。 

### R6: Missing Virtualization for Long Lists 缺少長列表的虛擬化

- **Severity**: IMPORTANT
- **Detection**: `.map(` rendering >100 items without virtual scrolling
- **CWV**: INP

使用 TanStack Virtual、react-window、Angular CDK Virtual Scroll 或 vue-virtual-scroller。

### R7: SSR of Immediately-Hidden Content 立即隱藏內容的 SSR

- **Severity**: SUGGESTION
- **Detection**: Server-rendering `display: none` components
- **CWV**: LCP (TTFB)

使用客戶端渲染模態框、抽屜、下拉菜單。 Angular: `@defer`。 React: `React.lazy`。

### R8: Missing `key` Prop on List Items 缺少列表項的 `key` 屬性

- **Severity**: IMPORTANT
- **Detection**: `.map(` without `key=` prop
- **CWV**: INP

```tsx
// GOOD — stable unique key
{items.map(item => <Row key={item.id} data={item} />)}
```

永遠不要在列表可以重新排序的情況下使用數組索引作為 key。

---

## JavaScript Runtime and INP Anti-Patterns (J1-J8) JavaScript 執行時和 INP 反模式

### J1: Long Synchronous Task in Event Handler 事件處理程序中的長同步任務

- **Severity**: CRITICAL
- **Detection**: Event handlers with heavy computation (>50ms)
- **CWV**: INP

```typescript
// GOOD — yield to browser
async function handleClick() {
  setLoading(true);
  await (globalThis.scheduler?.yield?.() ?? new Promise(r => setTimeout(r, 0)));
  const result = expensiveComputation(data);
  setResult(result);
}
```

將重工作移至 Web Worker 以獲得最佳效果。

> **注意:** `scheduler.yield()` 在 Chrome 129+、Firefox 129+ 支援，但截至 2026 年 4 月 Safari 不支援。回退方案: `await (globalThis.scheduler?.yield?.() ?? new Promise(r => setTimeout(r, 0)))`。

### J2: Layout Thrashing 布局抖動

- **Severity**: CRITICAL
- **Detection**: `offsetHeight|offsetWidth|getBoundingClientRect|clientHeight` in loops
- **CWV**: INP

```typescript
// GOOD — batch reads then batch writes
const heights = elements.map(el => el.offsetHeight);
elements.forEach((el, i) => { el.style.height = `${heights[i] + 10}px`; });
```

### J3: setInterval/setTimeout Without Cleanup 設置間隔/超時而不清理

- **Severity**: IMPORTANT
- **Detection**: `setInterval|setTimeout` without cleanup
- **Impact**: Memory

```tsx
useEffect(() => {
  const id = setInterval(() => fetchData(), 5000);
  return () => clearInterval(id);
}, []);
```

### J4: addEventListener Without removeEventListener 添加事件监听器而不移除

- **Severity**: IMPORTANT
- **Detection**: `addEventListener` without cleanup
- **Impact**: Memory

```tsx
useEffect(() => {
  const controller = new AbortController();
  window.addEventListener('resize', handleResize, { signal: controller.signal });
  return () => controller.abort();
}, []);
```

### J5: Detached DOM Node References 分離的 DOM 節點引用

- **Severity**: SUGGESTION
- **Detection**: Variables holding references to removed DOM elements
- **Impact**: Memory

移除元素時，將引用設置為 `null`。

### J6: Synchronous XHR 同步 XHR

- **Severity**: CRITICAL
- **Detection**: `XMLHttpRequest` with synchronous flag
- **CWV**: INP

使用 `fetch()`（始終異步）。

### J7: Heavy Computation on Main Thread 主線程上的重計算

- **Severity**: IMPORTANT
- **Detection**: CPU-intensive operations in component code
- **CWV**: INP

將重工作移至 Web Worker 或使用 `scheduler.yield()` 將其分塊。

### J8: Missing Effect Cleanup 缺少 Effect 清理

- **Severity**: IMPORTANT
- **Detection**: `useEffect` without return cleanup; `subscribe` without unsubscribe
- **Impact**: Memory

React: return cleanup from `useEffect`. Angular: `takeUntilDestroyed()`. Vue: `onUnmounted`.

---

## CSS Performance Anti-Patterns (C1-C7) CSS 效能反模式

### C1: Animation Using Layout-Triggering Properties 動畫使用觸發佈局的屬性

- **Severity**: CRITICAL
- **Detection**: `animation:|transition:` with `top|left|width|height|margin|padding`
- **CWV**: INP

```css
/* BAD — main thread, <60fps */
.card { transition: width 0.3s, height 0.3s; }

/* GOOD — GPU compositor, 60fps */
.card { transition: transform 0.3s, opacity 0.3s; }
.card:hover { transform: scale(1.05); }
```

### C2: Missing content-visibility for Off-Screen Sections 缺少離屏區域的 content-visibility

- **Severity**: SUGGESTION
- **Detection**: Long pages without `content-visibility: auto`
- **CWV**: INP

```css
.below-fold-section {
  content-visibility: auto;
  contain-intrinsic-size: auto 500px;
}
```

### C3: will-change Applied Permanently 永久應用 will-change

- **Severity**: SUGGESTION
- **Detection**: `will-change:` in base CSS (not `:hover|:focus`)
- **Impact**: Memory

僅在互動時應用，或讓瀏覽器自動優化。

### C4: Large Unused CSS 大量未使用的 CSS

- **Severity**: IMPORTANT
- **Detection**: CSS where >50% of rules are unused
- **CWV**: LCP

使用 PurgeCSS、Tailwind purge 或 critters。按路由拆分 CSS。

### C5: Universal Selector in Hot Paths 熱路徑中的通用選擇器

- **Severity**: SUGGESTION
- **Detection**: `\* \{` in CSS
- **CWV**: INP

```css
/* GOOD — zero-specificity reset */
:where(*, *::before, *::after) { box-sizing: border-box; }
```

### C6: Missing CSS Containment 缺少 CSS 包含

- **Severity**: SUGGESTION
- **Detection**: Complex components without `contain` property
- **CWV**: INP

```css
.sidebar { contain: layout style paint; }
```

### C7: Route Transitions Without View Transitions API 路由過渡缺少 View Transitions API

- **Severity**: SUGGESTION
- **Detection**: SPA route changes without View Transitions API
- **CWV**: CLS (perceived)

```javascript
// Use View Transitions for smooth route changes (with feature check)
if (document.startViewTransition) {
  document.startViewTransition(() => {
    // update DOM / navigate
  });
} else {
  // fallback: update DOM directly
}
```

同文件過渡在所有主要瀏覽器中受支持。跨文件過渡在 Chrome/Edge 126+、Safari 18.5+ 中受支持。在調用之前始終進行功能檢查——不支持的瀏覽器在沒有保護的情況下會拋出錯誤。

---

## Images, Media and Fonts Anti-Patterns (I1-I8) 圖像、媒體和字體反模式

### I1: Images Without Dimensions 沒有尺寸的圖像

- **Severity**: CRITICAL
- **Detection**: `<img` without `width=` and `height=`
- **CWV**: CLS

Always set `width` and `height` on images, or use `aspect-ratio` in CSS.

### I2: Lazy Loading Above-Fold Images 首屏圖像的懶加載

- **Severity**: CRITICAL
- **Detection**: `loading="lazy"` on hero/banner images
- **CWV**: LCP

```html
<!-- GOOD — eager load with high priority -->
<img src="/hero.webp" alt="Hero" fetchpriority="high" />
```

### I3: Legacy Format Only (JPEG/PNG) 僅使用傳統格式（JPEG/PNG）

- **Severity**: IMPORTANT
- **Detection**: Images without WebP/AVIF alternatives
- **CWV**: LCP

```html
<picture>
  <source srcset="/hero.avif" type="image/avif" />
  <source srcset="/hero.webp" type="image/webp" />
  <img src="/hero.jpg" alt="Hero" width="1200" height="600" />
</picture>
```

### I4: Missing Responsive srcset/sizes 缺少響應式 srcset/sizes

- **Severity**: IMPORTANT
- **Detection**: `<img` without `srcset`
- **CWV**: LCP

```html
<img src="/hero-800.jpg" alt="Hero"
     srcset="/hero-400.jpg 400w, /hero-800.jpg 800w, /hero-1200.jpg 1200w"
     sizes="(max-width: 600px) 400px, (max-width: 1024px) 800px, 1200px" />
```

### I5: Font Without font-display 缺少 font-display

- **Severity**: IMPORTANT
- **Detection**: `@font-face` without `font-display`
- **CWV**: CLS

```css
@font-face {
  font-family: 'CustomFont';
  src: url('/fonts/custom.woff2') format('woff2');
  font-display: swap; /* or "optional" for best CLS */
}
```

### I6: Critical Font Not Preloaded 重要字體未預加載

- **Severity**: IMPORTANT
- **Detection**: Custom font without `<link rel="preload">`
- **CWV**: LCP + CLS

```html
<link rel="preload" href="/fonts/main.woff2" as="font" type="font/woff2" crossorigin />
```

### I7: Full Font Loaded When Subset Suffices 當子集足夠時加載完整字體

- **Severity**: SUGGESTION
- **Detection**: Font files > 50KB WOFF2
- **CWV**: LCP

Use `unicode-range`, subset with glyphhanger, or `next/font` (auto-subsets Google Fonts).

### I8: Unoptimized SVGs 未優化的 SVG

- **Severity**: SUGGESTION
- **Detection**: SVGs with editor metadata
- **CWV**: LCP (minor)

```bash
npx svgo input.svg -o output.svg
```

---

## Bundle and Tree Shaking Anti-Patterns (B1-B6) 捆綁和樹搖反模式

### B1: Barrel File Importing Entire Module

- **Severity**: IMPORTANT
- **Detection**: `from '\.\/(?:.*\/index|components)'`
- **CWV**: INP

```typescript
// BAD
import { Button } from './components';

// GOOD — direct import
import { Button } from './components/Button';
```

### B2: CommonJS require() Preventing Tree Shaking ,CommonJS require() 阻止樹搖

- **Severity**: IMPORTANT
- **Detection**: `require(` in frontend code
- **CWV**: INP

Use ESM `import/export`. Replace `require` with `import`.

### B3: Large Dependency for Small Utility 大型依賴用於小型工具

- **Severity**: IMPORTANT
- **Detection**: `from "moment"|from "lodash"` (full imports)
- **CWV**: INP

```typescript
// GOOD — tree-shakeable alternatives
import { format } from 'date-fns';
import { pick } from 'lodash-es';

// BEST — native JS
const formatted = new Intl.DateTimeFormat('en').format(date);
```

### B4: Missing Dynamic Import for Route Splitting 缺少路由拆分的動態導入

- **Severity**: CRITICAL
- **Detection**: All route components imported statically
- **CWV**: INP

```tsx
// Next.js: automatic with file-based routing
// React:
const Page = React.lazy(() => import('./pages/Page'));
// Angular:
{ path: 'settings', loadComponent: () => import('./pages/settings.component') }
// Vue:
const Page = defineAsyncComponent(() => import('./pages/Page.vue'));
```

### B5: Missing sideEffects in package.json 缺少 package.json 中的 sideEffects 字段

- **Severity**: SUGGESTION
- **Detection**: Library package.json without `"sideEffects"` field
- **CWV**: INP

```json
{ "sideEffects": false }
```

### B6: Duplicate Dependencies 重複的依賴

- **Severity**: SUGGESTION
- **Detection**: Same library at multiple versions
- **CWV**: INP

```bash
npm dedupe
```

---

## Framework-Specific: Next.js (NX1-NX6) 框架特定：Next.js (NX1-NX6)

### NX1: Not Using next/image 未使用 next/image

- **Severity**: IMPORTANT
- **Detection**: `<img ` in `.tsx` instead of `<Image>`
- **CWV**: LCP + CLS

```tsx
import Image from 'next/image';
<Image src="/hero.jpg" alt="Hero" width={1200} height={600} priority />
```

### NX2: Not Using Cache Components for Partial Prerendering 未使用快取組件進行部分預渲染

- **Severity**: IMPORTANT
- **Detection**: Pages without `"use cache"` directive in Next.js 16+ projects
- **CWV**: LCP

```typescript
// BAD — entire page is dynamic
export default async function Page() {
  const data = await fetchData(); // blocks full page render
  return <div>{data.title}</div>;
}

// GOOD — enable Partial Prerendering with "use cache"
// next.config.ts: { cacheComponents: true }
"use cache";
export default async function Page() {
  const data = await fetchData(); // static shell renders instantly, dynamic holes stream
  return <div>{data.title}</div>;
}
```

在 next.config.ts 檔案中啟用 cacheComponents: true 。可以在檔案、元件或函數層級使用 "use cache" 。靜態 shell 會立即載入；動態內容會透過 Suspense 邊界進行串流傳輸。

### NX3: Unnecessary "use client" on Server-Renderable Component 在可由服務器渲染的組件上不必要的 "use client"

- **Severity**: IMPORTANT
- **Detection**: `"use client"` on components without hooks or browser APIs
- **CWV**: INP

移除僅渲染靜態內容的組件上的 `"use client"`。

### NX4: Data Fetching in useEffect Instead of Server-Side Fetching 在 useEffect 中進行數據抓取而不是服務器端抓取

- **Severity**: CRITICAL
- **Detection**: `useEffect` + `fetch` in Next.js App Router pages
- **CWV**: LCP

直接在服務器組件中抓取數據（異步函數體）。

### NX5: Missing next/font

- **Severity**: IMPORTANT
- **Detection**: `fonts.googleapis|fonts.gstatic` in CSS/HTML
- **CWV**: CLS + LCP

```tsx
import { Inter } from 'next/font/google';
const inter = Inter({ subsets: ['latin'] });
```

### NX6: Missing "use cache" for Cacheable Server Functions 缺少可快取的服務器函數的 "use cache"

- **Severity**: IMPORTANT
- **Detection**: Async server functions without `"use cache"` in Next.js 16+ with `cacheComponents: true`
- **CWV**: LCP

```typescript
// BAD — data fetched on every request
async function getProducts() {
  return await db.products.findMany();
}

// GOOD — cached with revalidation
"use cache";
import { cacheLife } from 'next/cache';
async function getProducts() {
  cacheLife('hours');
  return await db.products.findMany();
}
```

"use cache" 取代了舊的 unstable_cache 和 fetch 快取選項。使用 cacheLife() 和 cacheTag() 可以進行更精細的控制。

---

## Framework-Specific: Angular (NG1-NG6) 框架特定：Angular (NG1-NG6)

### NG1: Default Change Detection on Presentational Components 預設變更檢測在展示組件上

- **Severity**: IMPORTANT
- **Detection**: Components without `ChangeDetectionStrategy.OnPush` (Angular <19) or without signals (Angular 19+)
- **CWV**: INP

```typescript
// Angular <19: Use OnPush
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  ...
})

// Angular 19+: Prefer zoneless with signals
// app.config.ts: provideZonelessChangeDetection()
@Component({ ... })
export class ProductCard {
  product = input.required<Product>(); // signal input
  price = computed(() => this.product().price * 1.19); // derived signal
}
```

Angular 19+：建議使用基於訊號的無區域變更偵測。使用基於訊號的響應式時，無需使用 OnPush。 Angular 20+ 已穩定支援無區域變更檢測。

### NG2: Not Using NgOptimizedImage 未使用 NgOptimizedImage

- **Severity**: IMPORTANT
- **Detection**: `<img` without `ngSrc` in `.component.html`
- **CWV**: LCP + CLS

```html
<img ngSrc="/hero.jpg" alt="Hero" width="1200" height="600" priority />
```

### NG3: Missing @defer for Below-Fold Content 缺少 @defer 用於下折內容

- **Severity**: SUGGESTION
- **Detection**: Heavy below-fold components loaded eagerly (Angular 17+)
- **CWV**: INP

```html
@defer (on viewport) {
  <app-heavy-chart [data]="chartData" />
} @placeholder {
  <div class="chart-skeleton"></div>
}
```

### NG4: Not Using Signals for Reactive State 未使用訊號進行響應式狀態管理

- **Severity**: SUGGESTION
- **Detection**: Class properties without signals in Angular 19+
- **CWV**: INP

使用 signal() 處理回應式狀態， computed() 處理衍生值。訊號 API（ signal() 、 computed() 、 effect() ）自 Angular 20 起已穩定。

### NG5: Full Hydration Without Incremental Hydration 全量水合而無增量水合

- **Severity**: IMPORTANT
- **Detection**: SSR app without `withIncrementalHydration()` in Angular 19+
- **CWV**: LCP, INP

```typescript
// BAD — full hydration blocks interactivity
provideClientHydration()

// GOOD — incremental hydration with triggers
provideClientHydration(withIncrementalHydration())
```

使用 @defer 觸發器（在視窗或互動時）按需為組件注水。透過延遲非關鍵組件的注水來縮短 TTI。增量水合在 Angular 19+ 中可用，並在 Angular 20+ 中穩定。

### NG6: Still Using zone.js in Angular 20+ Projects 在 Angular 20+ 項目中仍使用 zone.js

- **Severity**: SUGGESTION
- **Detection**: `zone.js` in polyfills array, no `provideZonelessChangeDetection()` in Angular 20+
- **CWV**: INP

```typescript
// app.config.ts
export const appConfig = {
  providers: [
    provideZonelessChangeDetection(), // removes ~15-30KB from bundle
    // ...
  ]
};
```

利用訊號進行無區域變更偵測可以減少套件大小並提高運行時效能。自 Angular 20 起穩定可用。

---

## Framework-Specific: React (RX1-RX4)  框架特定：React (RX1-RX4)

### RX1: Missing React Compiler Adoption 缺少 React 編譯器採用

- **Severity**: SUGGESTION
- **Detection**: Manual `useMemo|useCallback` in React 19+ project
- **CWV**: INP

啟用 React Compiler (v19+) 以自動進行記憶化。移除手動包裝。

### RX2: Missing useTransition for Expensive Updates 缺少 useTransition 用於昂貴的更新

- **Severity**: IMPORTANT
- **Detection**: State updates causing expensive re-renders without `useTransition`
- **CWV**: INP

```tsx
const [isPending, startTransition] = useTransition();
function handleFilter(value) {
  startTransition(() => setFilter(value));
}
```

### RX3: Missing useDeferredValue for Expensive Rendering 缺少 useDeferredValue 用於昂貴的渲染

- **Severity**: IMPORTANT
- **Detection**: Expensive rendering from rapidly-changing input
- **CWV**: INP

```tsx
const deferredQuery = useDeferredValue(query);
const results = expensiveFilter(items, deferredQuery);
```

### RX4: Missing React.lazy for Route Splitting 缺少 React.lazy 用於路由拆分

- **Severity**: IMPORTANT
- **Detection**: Route components imported statically
- **CWV**: INP

```tsx
const Settings = React.lazy(() => import('./pages/Settings'));
```

---

## Framework-Specific: Vue (VU1-VU4) 框架特定：Vue (VU1-VU4)

### VU1: reactive() on Large Data Structures 在大型数据结构上使用 reactive()

- **Severity**: IMPORTANT
- **Detection**: `reactive(` on large arrays or deep objects
- **CWV**: INP

Use `shallowRef()` or `shallowReactive()` for large data.

### VU2: Missing v-memo on Expensive List Renders 缺少 v-memo 用於昂貴的列表渲染

- **Severity**: SUGGESTION
- **Detection**: Large lists without `v-memo`
- **CWV**: INP

```vue
<div v-for="item in items" :key="item.id" v-memo="[item.id, item.updatedAt]">
  <ExpensiveItem :data="item" />
</div>
```

### VU3: Missing defineAsyncComponent 缺少 defineAsyncComponent

- **Severity**: IMPORTANT
- **Detection**: Heavy components imported statically
- **CWV**: INP

```typescript
const HeavyChart = defineAsyncComponent(() => import('./HeavyChart.vue'));
```

### VU4: Not Using Vapor Mode for Performance-Critical Components 未對性能關鍵組件使用 Vapor 模式

- **Severity**: SUGGESTION
- **Detection**: Performance-critical components using virtual DOM in Vue 3.6+
- **CWV**: INP

Vue 3.6+ Vapor Mode 將模板編譯為直接的 DOM 操作，繞過虛擬 DOM。用於性能關鍵的子樹。可以與標準組件混合使用。

---

## Resource Hints Quick Reference 資源提示快速參考

| Hint | Purpose | When to Use |
|------|---------|-------------|
| `preconnect` | DNS + TCP + TLS 早期 | 關鍵第三方來源（API、CDN、字體） |
| `preload` | 立即抓取，高優先級 | LCP 圖片，關鍵字體 |
| `prefetch` | 低優先級，用於未來導航 | 下一頁資源 |
| `dns-prefetch` | 僅 DNS 解析 | 非關鍵第三方來源 |
| `modulepreload` | 預加載 + 解析 ES 模組 | 關鍵 JS 模組 |
| `<script type="speculationrules">` | 預抓取/預渲染下一次導航 | 可能的下一頁（Chrome 121+，漸進增強） |

---

## Image Optimization Quick Reference 圖片優化快速參考

| Aspect | Recommendation |
|--------|---------------|
| Format | WebP (25-34% smaller), AVIF (50% smaller) |
| LCP image | `fetchpriority="high"` or framework `priority` prop |
| Below-fold | `loading="lazy"` |
| Dimensions | Always set `width` + `height` |
| Responsive | `srcset` + `sizes` or framework Image component |
| Compression | Quality 75-85 for photos |

---

## Font Loading Quick Reference 字體加載快速參考

| Strategy | Best For | CLS Impact |
|----------|---------|-----------|
| `font-display: swap` | Body text | Slight FOUT, minimal CLS |
| `font-display: optional` | All fonts (best CLS) | No FOUT, no CLS |
| `next/font` | Next.js projects | Zero CLS |
| Variable fonts | Multiple weights | Single file for all weights |

規則：僅預先載入 1-2 個關鍵字體，使用 WOFF2，僅載入所需字符，盡可能自行託管。

---

## Performance Checklist (CWV) 性能檢查表（CWV）

### LCP (< 2.5s)
- [ ] LCP 影像具有 fetchpriority="high" 或 priority 屬性
- [ ] LCP 影像已預加載（如果不在 HTML 源中）
- [ ] 上折疊影像不使用 `loading="lazy"`
- [ ] 關鍵 CSS 已內聯或提取
- [ ] 無阻塞渲染的腳本（使用 `defer` 或 `async`）
- [ ] 預連接到關鍵第三方來源
- [ ] 主內容已伺服器端渲染（非客戶端抓取）
- [ ] 影像使用現代格式（WebP/AVIF）並具響應式 `srcset`
- [ ] 啟用壓縮（優先使用 Brotli）
- [ ] 字體已預加載並使用 `font-display: swap` 或 `optional`

### INP (< 200ms)
- [ ] 事件處理器在 < 50ms 內完成
- [ ] 長任務拆分為較小的塊
- [ ] 實施基於路由的代碼拆分
- [ ] 將重計算移至 Web Workers
- [ ] 列表項目超過 100 個時使用虛擬化
- [ ] 不使用桶文件導入（直接導入組件）
- [ ] 使用 ESM 導入（而非 CommonJS `require`）
- [ ] 僅在需要互動的組件上使用 `"use client"`
- [ ] 不對觸發佈局的 CSS 屬性進行動畫
- [ ] 實施效果清理（避免洩漏的監聽器/計時器）

### CLS (< 0.1)
- [ ] 所有影像都有 `width` 和 `height` 屬性
- [ ] 字體使用 `font-display: swap` 或 `optional`
- [ ] 不在現有內容上方動態注入內容
- [ ] 廣告/嵌入內容有保留空間
- [ ] 無水合不匹配
- [ ] `content-visibility: auto` 具有 `contain-intrinsic-size`
