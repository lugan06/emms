<template>
  <div class="user-layout">
    <header class="user-header">
      <RouterLink class="brand" to="/">EEMS</RouterLink>
      <span class="layout-label">Public portal</span>
    </header>
    <main class="user-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.user-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 32px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.brand {
  color: #2563eb;
  font-size: 20px;
  font-weight: 700;
}

.layout-label {
  color: #6b7280;
  font-size: 13px;
}

.user-main {
  padding: 40px 32px;
}
</style>
