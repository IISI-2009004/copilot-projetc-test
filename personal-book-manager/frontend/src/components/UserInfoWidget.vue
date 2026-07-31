<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()

const username = computed(() => userStore.currentUser?.username ?? '訪客')

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/')
  }
}
</script>

<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <span class="user-info">
      <el-icon><User /></el-icon>
      <span class="user-info__name">{{ username }}</span>
      <el-icon class="user-info__arrow"><ArrowDown /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item disabled>使用者：{{ username }}</el-dropdown-item>
        <el-dropdown-item command="logout" divided>登出</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.user-info {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: var(--el-text-color-primary);
}

.user-info__name {
  font-weight: 500;
}

.user-info__arrow {
  font-size: 12px;
}
</style>
