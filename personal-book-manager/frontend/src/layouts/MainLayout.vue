<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue'
</script>

<template>
  <div class="main-layout">
    <AppSidebar />
    <main class="main-layout__content">
      <RouterView />
    </main>
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
</style>
