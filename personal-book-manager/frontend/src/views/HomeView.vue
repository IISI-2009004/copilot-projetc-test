<script setup lang="ts">
import { computed } from 'vue'
import { Notebook, Timer, CircleCheck, Calendar } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { mockReadingStats } from '@/mocks/mockData'

const userStore = useUserStore()
const stats = mockReadingStats

const greeting = computed(() => `歡迎回來，${userStore.currentUser?.username ?? '訪客'}`)

const cards = [
  { label: '藏書總數', value: stats.totalBooks, unit: '本', icon: Notebook },
  { label: '累計閱讀時長', value: stats.totalMinutes, unit: '分鐘', icon: Timer },
  { label: '已完成書籍', value: stats.completedBookCount, unit: '本', icon: CircleCheck },
  { label: '本月閱讀時長', value: stats.monthlyReadingMinutes, unit: '分鐘', icon: Calendar },
]
</script>

<template>
  <div class="home-view">
    <h2 class="home-view__greeting">{{ greeting }}</h2>
    <p class="home-view__hint">
      以下統計資料為前端 Scaffold 階段的 mock 資料，待後端 reading 統計 API 完成後將改接真實數據。
    </p>

    <el-row :gutter="16" class="home-view__cards">
      <el-col v-for="card in cards" :key="card.label" :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card__body">
            <el-icon class="stat-card__icon"><component :is="card.icon" /></el-icon>
            <div>
              <div class="stat-card__value">{{ card.value }}<span class="stat-card__unit">{{ card.unit }}</span></div>
              <div class="stat-card__label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.home-view__greeting {
  margin: 0 0 4px;
}

.home-view__hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin: 0 0 20px;
}

.home-view__cards {
  margin-top: 8px;
}

.stat-card {
  margin-bottom: 16px;
}

.stat-card__body {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-card__icon {
  font-size: 28px;
  color: var(--el-color-primary);
}

.stat-card__value {
  font-size: 22px;
  font-weight: 600;
}

.stat-card__unit {
  font-size: 13px;
  font-weight: 400;
  margin-left: 4px;
  color: var(--el-text-color-secondary);
}

.stat-card__label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
