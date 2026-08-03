import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // 開發時 frontend（vite dev server, 5173）與 backend（Spring Boot, 8080）分開啟動，
    // 透過 proxy 轉發 /api 請求避免瀏覽器 CORS 限制；正式環境由 backend WAR 直接
    // 提供打包後的靜態檔案，同源不需要 proxy。
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
