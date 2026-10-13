<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <RouterLink class="brand" to="/admin">EEMS 管理后台</RouterLink>
      <nav class="admin-nav">
        <RouterLink to="/admin">
          <el-icon><Odometer /></el-icon>
          <span>控制台</span>
        </RouterLink>
        <RouterLink to="/admin/exhibitions">
          <el-icon><Calendar /></el-icon>
          <span>展会管理</span>
        </RouterLink>
        <RouterLink to="/admin/categories">
          <el-icon><Menu /></el-icon>
          <span>栏目管理</span>
        </RouterLink>
        <RouterLink to="/admin/contents">
          <el-icon><Document /></el-icon>
          <span>内容管理</span>
        </RouterLink>
      </nav>
    </aside>
    <section class="admin-content">
      <header class="admin-header">
        <div>
          <p class="header-eyebrow">EEMS ADMINISTRATION</p>
          <strong>{{ route.meta.title || '管理后台' }}</strong>
        </div>
        <div class="admin-profile">
          <div class="profile-copy">
            <strong>{{ userStore.user?.nickname || userStore.user?.username || '管理员' }}</strong>
            <span>{{ userStore.user?.roleCode || 'ADMIN' }}</span>
          </div>
          <el-button text :icon="SwitchButton" @click="logout">退出登录</el-button>
        </div>
      </header>
      <main class="admin-main">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { Calendar, Document, Menu, Odometer, SwitchButton } from '@element-plus/icons-vue'

import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
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

.admin-nav a {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  color: #d1d5db;
  text-align: left;
  background: transparent;
  border: 0;
  border-radius: 4px;
  cursor: pointer;
}

.admin-nav a.router-link-active,
.admin-nav a:hover {
  color: #ffffff;
  background: #374151;
}

.admin-nav .el-icon {
  font-size: 16px;
}

.admin-content {
  flex: 1;
  min-width: 0;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  gap: 24px;
  padding: 14px 32px;
  color: #374151;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.header-eyebrow {
  margin: 0 0 3px;
  color: #9ca3af;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.1em;
}

.admin-profile {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-copy {
  display: grid;
  gap: 2px;
  text-align: right;
}

.profile-copy span {
  color: #9ca3af;
  font-size: 12px;
}

.admin-main {
  padding: 32px;
}

@media (max-width: 720px) {
  .admin-sidebar {
    flex-basis: 180px;
  }

  .admin-header {
    padding: 14px 20px;
  }

  .admin-main {
    padding: 20px;
  }
}

@media (max-width: 560px) {
  .admin-layout {
    display: block;
  }

  .admin-sidebar {
    min-height: auto;
    padding: 14px 16px;
  }

  .admin-nav {
    display: flex;
    overflow-x: auto;
    margin-top: 16px;
  }

  .admin-nav a {
    flex: 0 0 auto;
  }

  .admin-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .profile-copy {
    text-align: left;
  }
}
</style>
