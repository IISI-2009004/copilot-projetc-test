<script setup lang="ts">
import { useRoute } from 'vue-router'
import { HomeFilled, Reading, Notebook } from '@element-plus/icons-vue'

const route = useRoute()
</script>

<template>
  <el-menu :default-active="route.path" mode="horizontal" router class="app-menu" :ellipsis="false">
    <el-menu-item index="/">
      <el-icon><HomeFilled /></el-icon>
      <span>首頁</span>
    </el-menu-item>
    <el-menu-item index="/books">
      <el-icon><Notebook /></el-icon>
      <span>藏書管理</span>
    </el-menu-item>
    <el-menu-item index="/reading">
      <el-icon><Reading /></el-icon>
      <span>閱讀記錄</span>
    </el-menu-item>
  </el-menu>
</template>

<style scoped>
.app-menu {
  flex: 1;
}
</style>
