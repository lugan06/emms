<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <RouterLink class="brand" to="/admin">EEMS Admin</RouterLink>
      <nav class="admin-nav">
        <RouterLink to="/admin">Dashboard</RouterLink>
      </nav>
      <button class="logout-button" type="button" @click="logout">Sign out</button>
    </aside>
    <section class="admin-content">
      <header class="admin-header">Administration</header>
      <main class="admin-main">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'

import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function logout(): void {
  userStore.clearAuth()
  void router.replace({ name: 'admin-login' })
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f3f4f6;
}

.admin-sidebar {
  display: flex;
  flex: 0 0 224px;
  flex-direction: column;
  min-height: 100vh;
  padding: 24px 16px;
  color: #ffffff;
  background: #1f2937;
}

.brand {
  padding: 0 12px;
  font-size: 18px;
  font-weight: 700;
}

.admin-nav {
  display: grid;
  gap: 4px;
  margin-top: 32px;
}

.admin-nav a,
.logout-button {
  padding: 10px 12px;
  color: #d1d5db;
  text-align: left;
  background: transparent;
  border: 0;
  border-radius: 4px;
  cursor: pointer;
}

.admin-nav a.router-link-active,
.admin-nav a:hover,
.logout-button:hover {
  color: #ffffff;
  background: #374151;
}

.logout-button {
  margin-top: auto;
}

.admin-content {
  flex: 1;
  min-width: 0;
}

.admin-header {
  height: 64px;
  padding: 21px 32px;
  color: #374151;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.admin-main {
  padding: 32px;
}
</style>
