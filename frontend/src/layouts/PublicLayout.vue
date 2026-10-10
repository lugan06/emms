<template>
  <div class="user-layout">
    <header class="user-header">
      <div class="header-inner">
        <RouterLink class="brand" to="/">
          <img v-if="publicStore.site?.logoUrl" :src="publicStore.site.logoUrl" :alt="publicStore.site.siteTitle || 'EEMS'" />
          <span>{{ publicStore.site?.siteTitle || 'EEMS' }}</span>
        </RouterLink>
        <PublicCategoryNavigation :categories="publicStore.categories" :loading="publicStore.navigationLoading" />
        <RouterLink class="exhibition-link" to="/exhibitions">展会</RouterLink>
      </div>
    </header>

    <main class="user-main">
      <RouterView />
    </main>

    <footer class="user-footer">
      <div class="footer-inner">
        <div>
          <strong>{{ publicStore.site?.siteTitle || 'EEMS' }}</strong>
          <p v-if="publicStore.site?.footerInfo">{{ publicStore.site.footerInfo }}</p>
          <p v-if="publicStore.site?.icpNumber">{{ publicStore.site.icpNumber }}</p>
        </div>
        <div v-if="publicStore.company" class="company-summary">
          <strong>{{ publicStore.company.companyName }}</strong>
          <p v-if="publicStore.company.address">{{ publicStore.company.address }}</p>
          <p v-if="publicStore.company.telephone">电话：{{ publicStore.company.telephone }}</p>
          <p v-if="publicStore.company.email">邮箱：{{ publicStore.company.email }}</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterLink, RouterView } from 'vue-router'

import PublicCategoryNavigation from '@/components/public/PublicCategoryNavigation.vue'
import { usePublicStore } from '@/stores/public'

const publicStore = usePublicStore()

onMounted(() => {
  void publicStore.loadNavigation()
})
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.user-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgb(255 255 255 / 96%);
  border-bottom: 1px solid #e5e7eb;
  backdrop-filter: blur(10px);
}

.header-inner,
.user-main,
.footer-inner {
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
}

.header-inner {
  display: flex;
  align-items: center;
  min-height: 68px;
  gap: 32px;
}

.brand {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 10px;
  color: #111827;
  font-size: 19px;
  font-weight: 750;
  white-space: nowrap;
}

.brand img {
  width: 34px;
  height: 34px;
  object-fit: contain;
}

.exhibition-link {
  flex: 0 0 auto;
  margin-left: auto;
  padding: 9px 14px;
  color: #ffffff;
  font-size: 13px;
  background: #2563eb;
  border-radius: 4px;
}

.user-main {
  min-height: calc(100vh - 260px);
  padding: 40px 0 72px;
}

.user-footer {
  color: #d1d5db;
  background: #1f2937;
}

.footer-inner {
  display: flex;
  justify-content: space-between;
  gap: 40px;
  padding: 36px 0;
}

.footer-inner strong {
  color: #ffffff;
  font-size: 15px;
}

.footer-inner p {
  margin: 8px 0 0;
  color: #9ca3af;
  font-size: 13px;
  line-height: 1.6;
}

.company-summary {
  text-align: right;
}

@media (max-width: 760px) {
  .header-inner,
  .user-main,
  .footer-inner {
    width: min(100% - 32px, 600px);
  }

  .header-inner {
    flex-wrap: wrap;
    gap: 12px 18px;
    padding: 12px 0;
  }

  .category-navigation {
    order: 3;
    flex: 1 0 100%;
  }

  .exhibition-link {
    margin-left: auto;
  }

  .user-main {
    padding-top: 24px;
  }

  .footer-inner {
    flex-direction: column;
  }

  .company-summary {
    text-align: left;
  }
}
</style>
