<template>
  <nav class="category-navigation" aria-label="公共栏目导航">
    <el-skeleton v-if="loading" :rows="1" animated class="navigation-skeleton" />
    <template v-else>
      <template v-for="category in categories" :key="category.id">
        <el-dropdown v-if="category.children.length > 0" trigger="hover">
          <span class="nav-item nav-dropdown-item">
            {{ category.name }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="child in category.children" :key="child.id">
                <RouterLink v-if="getInternalPath(child)" :to="getInternalPath(child)!">{{ child.name }}</RouterLink>
                <a v-else-if="isExternalLink(child.linkUrl)" :href="child.linkUrl">{{ child.name }}</a>
                <button v-else type="button" class="nav-link" @click="handleUnavailableLink">
                  {{ child.name }}
                </button>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <RouterLink v-else-if="getInternalPath(category)" :to="getInternalPath(category)!" class="nav-item">
          {{ category.name }}
        </RouterLink>
        <a v-else-if="isExternalLink(category.linkUrl)" :href="category.linkUrl" class="nav-item">
          {{ category.name }}
        </a>
        <button v-else type="button" class="nav-item nav-link" @click="handleUnavailableLink">
          {{ category.name }}
        </button>
      </template>
    </template>
  </nav>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import type { PublicCategory } from '@/types/public'

defineProps<{
  categories: PublicCategory[]
  loading: boolean
}>()

function getInternalPath(category: PublicCategory): string | null {
  const keys = [category.code, category.urlName]
    .filter((value): value is string => Boolean(value))
    .map((value) => value.toLowerCase())
  if (keys.some((key) => ['exhibition', 'exhibitions', 'exhibition-overview', 'overview'].includes(key))) {
    return '/exhibitions'
  }
  return null
}

function isExternalLink(linkUrl?: string | null): linkUrl is string {
  return Boolean(linkUrl && !linkUrl.startsWith('/'))
}

function handleUnavailableLink(): void {
  ElMessage.info('该功能即将上线')
}
</script>

<style scoped>
.category-navigation {
  display: flex;
  align-items: center;
  gap: 28px;
  min-width: 0;
}

.navigation-skeleton {
  width: 260px;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 0;
  color: #4b5563;
  font-size: 14px;
  white-space: nowrap;
  cursor: pointer;
}

.nav-item:hover,
.nav-item.router-link-active {
  color: #2563eb;
}

.nav-dropdown-item:focus {
  outline: none;
}

.el-dropdown-menu a {
  color: inherit;
}

.nav-link {
  font: inherit;
  text-align: left;
  background: transparent;
  border: 0;
}

.el-dropdown-menu .nav-link {
  width: 100%;
  padding: 0;
  color: inherit;
  cursor: pointer;
}

@media (max-width: 760px) {
  .category-navigation {
    gap: 16px;
    overflow-x: auto;
  }
}
</style>
