<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()

const username = computed(() => userStore.currentUser?.username ?? '訪客')
const avatarLetter = computed(() => username.value.charAt(0).toUpperCase())

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/')
  }
}
</script>

<template>
  <el-dropdown trigger="click" class="user-widget" @command="handleCommand">
    <span class="user-widget__trigger">
      <el-avatar :size="28" class="user-widget__avatar">{{ avatarLetter }}</el-avatar>
      <span class="user-widget__name">{{ username }}</span>
      <el-icon class="user-widget__arrow"><ArrowRight /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item disabled>
          <el-icon><User /></el-icon>
          {{ username }}
        </el-dropdown-item>
        <el-dropdown-item command="logout" divided>登出</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.user-widget {
  display: block;
  width: 100%;
}

.user-widget__trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #e4e5ea;
  padding: 6px 4px;
  border-radius: 6px;
}

.user-widget__trigger:hover {
  background-color: #2b2d33;
}

.user-widget__avatar {
  background-color: var(--el-color-primary);
  font-size: 13px;
  flex-shrink: 0;
}

.user-widget__name {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-widget__arrow {
  font-size: 12px;
  color: #7c7e88;
}
</style>

