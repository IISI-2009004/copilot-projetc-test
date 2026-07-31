<script setup lang="ts">
import { useRoute } from 'vue-router'
import { HomeFilled, Reading, Files, FolderOpened, Notebook } from '@element-plus/icons-vue'
import { mockCategories } from '@/mocks/mockData'
import UserInfoWidget from '@/components/UserInfoWidget.vue'

const route = useRoute()

</script>

<template>
  <!-- 參考 Eagle 圖片管理工具的左側深色導覽列：頂部固定導覽項目、中段分類樹狀、底部使用者資訊 -->
  <aside class="app-sidebar">
    <div class="app-sidebar__brand">
      <el-icon :size="20"><Files /></el-icon>
      <span>個人圖書館</span>
    </div>

    <el-menu
      :default-active="route.path"
      router
      class="app-sidebar__menu"
      background-color="transparent"
      text-color="#c7c9d1"
      active-text-color="#ffffff"
    >
      <el-menu-item index="/">
        <el-icon><HomeFilled /></el-icon>
        <span>首頁</span>
      </el-menu-item>
      <el-menu-item index="/books">
        <el-icon><Notebook /></el-icon>
        <span>全部藏書</span>
      </el-menu-item>
      <el-menu-item index="/reading">
        <el-icon><Reading /></el-icon>
        <span>閱讀記錄</span>
      </el-menu-item>
    </el-menu>

    <div class="app-sidebar__section-title">分類</div>
    <el-scrollbar class="app-sidebar__categories">
      <el-tree
        :data="mockCategories"
        node-key="id"
        :props="{ label: 'label', children: 'children' }"
        default-expand-all
        class="app-sidebar__tree"
      >
        <template #default="{ node }">
          <span class="app-sidebar__tree-node">
            <el-icon><FolderOpened /></el-icon>
            <span>{{ node.label }}</span>
          </span>
        </template>
      </el-tree>
    </el-scrollbar>

    <div class="app-sidebar__footer">
      <UserInfoWidget />
    </div>
  </aside>
</template>

<style scoped>
.app-sidebar {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 232px;
  background-color: #1f2024;
  color: #c7c9d1;
  flex-shrink: 0;
}

.app-sidebar__brand {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
}

.app-sidebar__menu {
  border-right: none;
}

.app-sidebar__section-title {
  padding: 12px 16px 4px;
  font-size: 12px;
  color: #7c7e88;
  letter-spacing: 1px;
}

.app-sidebar__categories {
  flex: 1;
  min-height: 0;
  padding: 0 8px;
}

.app-sidebar__tree {
  background-color: transparent;
  --el-tree-node-hover-bg-color: #2b2d33;
  --el-tree-text-color: #c7c9d1;
}

.app-sidebar__tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
}

.app-sidebar__footer {
  border-top: 1px solid #2b2d33;
  padding: 8px 12px;
}
</style>
