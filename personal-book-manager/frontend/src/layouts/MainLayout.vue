<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue'
</script>

<template>
  <div class="main-layout">
    <AppSidebar />
    <main class="main-layout__content">
      <RouterView />
    </main>
    <!-- 詳細資訊面板的實際掛載點：與側邊欄一樣固定滿版高度，不隨主內容區捲動；
         各頁面（如 BookListView）透過 <Teleport to="#detail-panel-outlet"> 將面板內容傳送至此。 -->
    <div id="detail-panel-outlet" class="main-layout__detail-outlet"></div>
  </div>
</template>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
}

.main-layout__content {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  background-color: #f5f5f7;
  padding: 24px 32px;
}

.main-layout__detail-outlet:empty {
  display: none;
}
</style>
