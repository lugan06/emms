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
                <a v-if="child.linkUrl" :href="child.linkUrl">{{ child.name }}</a>
                <RouterLink v-else-if="getInternalPath(child)" :to="getInternalPath(child)!">{{ child.name }}</RouterLink>
                <span v-else>{{ child.name }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <a v-else-if="category.linkUrl" :href="category.linkUrl" class="nav-item">{{ category.name }}</a>
        <RouterLink v-else-if="getInternalPath(category)" :to="getInternalPath(category)!" class="nav-item">
          {{ category.name }}
        </RouterLink>
        <span v-else class="nav-item">{{ category.name }}</span>
      </template>
    </template>
  </nav>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'

import type { PublicCategory } from '@/types/public'

defineProps<{
  categories: PublicCategory[]
  loading: boolean
}>()

function getInternalPath(category: PublicCategory): string | null {
  const key = (category.urlName || category.code).toLowerCase()
  if (key === 'exhibition' || key === 'exhibitions') {
    return '/exhibitions'
  }
  return null
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

@media (max-width: 760px) {
  .category-navigation {
    gap: 16px;
    overflow-x: auto;
  }
}
</style>
