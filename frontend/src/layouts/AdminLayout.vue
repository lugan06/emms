<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <RouterLink class="brand" to="/admin">EEMS 管理后台</RouterLink>
      <nav class="admin-nav">
        <RouterLink to="/admin" :class="{ 'menu-active': isAdminMenuActive(route.path, '/admin') }">
          <el-icon><Odometer /></el-icon>
          <span>控制台</span>
        </RouterLink>
        <RouterLink to="/admin/exhibitions" :class="{ 'menu-active': isAdminMenuActive(route.path, '/admin/exhibitions') }">
          <el-icon><Calendar /></el-icon>
          <span>展会管理</span>
        </RouterLink>
        <RouterLink to="/admin/categories" :class="{ 'menu-active': isAdminMenuActive(route.path, '/admin/categories') }">
          <el-icon><Menu /></el-icon>
          <span>栏目管理</span>
        </RouterLink>
        <RouterLink to="/admin/contents" :class="{ 'menu-active': isAdminMenuActive(route.path, '/admin/contents') }">
          <el-icon><Document /></el-icon>
          <span>内容管理</span>
        </RouterLink>
        <span class="nav-group">系统配置</span>
        <RouterLink to="/admin/site" :class="{ 'menu-active': isAdminMenuActive(route.path, '/admin/site') }"><el-icon><Setting /></el-icon><span>站点配置</span></RouterLink>
        <RouterLink to="/admin/company" :class="{ 'menu-active': isAdminMenuActive(route.path, '/admin/company') }"><el-icon><OfficeBuilding /></el-icon><span>公司信息</span></RouterLink>
        <RouterLink to="/admin/guestbooks" :class="{ 'menu-active': isAdminMenuActive(route.path, '/admin/guestbooks') }"><el-icon><ChatDotRound /></el-icon><span>留言管理</span></RouterLink>
        <RouterLink v-if="isSuperAdmin(userStore.user)" to="/admin/logs" :class="{ 'menu-active': isAdminMenuActive(route.path, '/admin/logs') }"><el-icon><Tickets /></el-icon><span>日志管理</span></RouterLink>
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
          <el-button text @click="passwordDialogVisible = true">修改密码</el-button>
          <el-button text :icon="SwitchButton" @click="logout">退出登录</el-button>
        </div>
      </header>
      <main class="admin-main">
        <RouterView />
      </main>
    </section>
    <PasswordChangeDialog v-model="passwordDialogVisible" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { Calendar, Document, Menu, Odometer, SwitchButton, Setting, OfficeBuilding, ChatDotRound, Tickets } from '@element-plus/icons-vue'
import { isAdminMenuActive, isSuperAdmin } from '@/utils/system'

import { useUserStore } from '@/stores/user'
import PasswordChangeDialog from '@/components/admin/PasswordChangeDialog.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const passwordDialogVisible = ref(false)

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
  margin-top: 36px;
}

.admin-nav a {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  padding: 10px 14px;
  overflow: hidden;
  color: #c4cfdd;
  text-align: left;
  background: rgb(255 255 255 / 3%);
  border: 1.5px solid rgb(236 237 236 / 16%);
  border-radius: 10px;
  cursor: pointer;
  transform: translateY(0);
  transition: color 180ms ease, background 180ms ease, border-color 180ms ease,
    box-shadow 180ms ease, transform 180ms ease;
}

.admin-nav a:hover {
  color: #ffffff;
  background: #252727;
  border-color: #2d79f3;
  box-shadow: 0 8px 18px rgb(2 6 23 / 18%);
  transform: translateY(-1px);
}

.admin-nav a::before {
  position: absolute;
  top: 9px;
  bottom: 9px;
  left: 0;
  width: 3px;
  content: '';
  background: #60a5fa;
  border-radius: 0 3px 3px 0;
  opacity: 0;
  transform: scaleY(0.4);
  transition: opacity 180ms ease, transform 180ms ease;
}

.admin-nav a.menu-active {
  color: #ffffff;
  background: #151717;
  border-color: #2d79f3;
  box-shadow: 0 10px 22px rgb(15 23 42 / 28%), inset 0 1px 0 rgb(255 255 255 / 10%);
}

.admin-nav a.menu-active::before,
.admin-nav a:hover::before {
  opacity: 1;
  transform: scaleY(1);
}

.admin-nav .el-icon {
  flex: 0 0 auto;
  font-size: 16px;
  color: #93c5fd;
  transition: color 180ms ease, transform 180ms ease;
}

.admin-nav a:hover .el-icon,
.admin-nav a.menu-active .el-icon {
  color: #ffffff;
  transform: translateX(1px) scale(1.05);
}

.nav-group {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 4px 4px;
  color: #8191a7;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.nav-group::after {
  flex: 1;
  height: 1px;
  content: '';
  background: rgb(148 163 184 / 18%);
}

.admin-content {
  position: relative;
  flex: 1;
  min-width: 0;
  isolation: isolate;
  overflow: hidden;
  background: #f8fafc;
}

.admin-content::before {
  position: absolute;
  inset: 0;
  z-index: -1;
  pointer-events: none;
  content: '';
  background-image: linear-gradient(to right, #e2e8f0 1px, transparent 1px),
    linear-gradient(to bottom, #e2e8f0 1px, transparent 1px);
  background-size: 20px 30px;
  -webkit-mask-image: radial-gradient(ellipse 70% 60% at 50% 0%, #000 60%, transparent 100%);
  mask-image: radial-gradient(ellipse 70% 60% at 50% 0%, #000 60%, transparent 100%);
  opacity: 0.72;
}

.admin-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  gap: 24px;
  padding: 14px 32px;
  color: #374151;
  background: rgb(255 255 255 / 92%);
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
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 72px);
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
