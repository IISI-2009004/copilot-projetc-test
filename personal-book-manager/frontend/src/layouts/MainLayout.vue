<script setup lang="ts">
import AppMenu from '@/components/AppMenu.vue'
import UserInfoWidget from '@/components/UserInfoWidget.vue'
</script>

<template>
  <el-container class="main-layout">
    <el-header class="main-layout__header">
      <span class="main-layout__title">個人圖書管理系統</span>
      <AppMenu />
      <UserInfoWidget />
    </el-header>
    <el-main class="main-layout__main">
      <RouterView />
    </el-main>
  </el-container>
</template>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.main-layout__header {
  display: flex;
  align-items: center;
  gap: 24px;
  border-bottom: 1px solid var(--el-border-color);
  padding: 0 20px;
}

.main-layout__title {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.main-layout__main {
  background-color: var(--el-fill-color-blank);
}
</style>
