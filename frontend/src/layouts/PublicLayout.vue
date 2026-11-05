<template>
  <div class="user-layout">
    <header class="user-header">
      <div class="header-inner">
        <RouterLink class="brand" to="/">
          <SiteLogo :logo-url="publicStore.site?.logoUrl" :title="publicStore.site?.siteTitle" />
          <span>{{ publicStore.site?.siteTitle || 'EEMS' }}</span>
        </RouterLink>
        <PublicCategoryNavigation :categories="publicStore.categories" :loading="publicStore.navigationLoading" />
        <RouterLink class="exhibition-link" to="/exhibitions">展会</RouterLink>
      </div>
    </header>

    <main class="user-main">
      <RouterView />
    </main>

    <FloatingContact />

    <footer class="user-footer">
      <div class="footer-inner">
        <div class="footer-row footer-organizations">
          <div v-if="publicStore.site?.siteTitle" class="footer-item">
            <strong>主办单位</strong>
            <span>{{ publicStore.site.siteTitle }}</span>
          </div>
          <div v-if="publicStore.company?.companyName" class="footer-item">
            <strong>承办单位</strong>
            <span>{{ publicStore.company.companyName }}</span>
          </div>
        </div>
        <div v-if="publicStore.company" class="footer-row footer-contacts">
          <div v-if="publicStore.company.telephone || publicStore.company.mobile" class="footer-item">
            <strong>电话</strong>
            <a :href="`tel:${publicStore.company.telephone || publicStore.company.mobile}`">
              {{ publicStore.company.telephone || publicStore.company.mobile }}
            </a>
          </div>
          <div v-if="publicStore.company.contactName" class="footer-item">
            <strong>联系人</strong>
            <span>{{ publicStore.company.contactName }}</span>
          </div>
          <div v-if="publicStore.company.email" class="footer-item">
            <strong>邮箱</strong>
            <a :href="`mailto:${publicStore.company.email}`">{{ publicStore.company.email }}</a>
          </div>
        </div>
        <div class="footer-row footer-meta">
          <span>© {{ copyrightYear }} {{ publicStore.company?.companyName || publicStore.site?.siteTitle || 'EEMS' }}</span>
          <a
            v-if="publicStore.site?.icpNumber"
            href="https://beian.miit.gov.cn/"
            target="_blank"
            rel="noopener noreferrer"
          >{{ publicStore.site.icpNumber }}</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterLink, RouterView } from 'vue-router'

import PublicCategoryNavigation from '@/components/public/PublicCategoryNavigation.vue'
import FloatingContact from '@/components/public/FloatingContact.vue'
import SiteLogo from '@/components/public/SiteLogo.vue'
import { usePublicSmoothScroll } from '@/composables/usePublicSmoothScroll'
import { usePublicStore } from '@/stores/public'
import '@/styles/public-motion.css'

const publicStore = usePublicStore()
const copyrightYear = new Date().getFullYear()
usePublicSmoothScroll()

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
  background: rgb(255 255 255 / 80%);
  border-bottom: 1px solid rgb(148 163 184 / 24%);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
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
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 36px 0;
}

.footer-row {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px 28px;
  width: 100%;
}

.footer-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #d1d5db;
  font-size: 13px;
}

.footer-item strong {
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
}

.footer-item a,
.footer-meta a {
  color: inherit;
  text-decoration: none;
}

.footer-item a:hover,
.footer-meta a:hover {
  color: #ffffff;
}

.footer-meta {
  gap: 8px 20px;
  padding-top: 12px;
  border-top: 1px solid rgb(255 255 255 / 12%);
  color: #9ca3af;
  font-size: 12px;
  line-height: 1.5;
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
    align-items: stretch;
  }

  .footer-row {
    align-items: flex-start;
    justify-content: flex-start;
    gap: 10px 18px;
  }

  .footer-meta {
    justify-content: center;
  }
}
</style>
