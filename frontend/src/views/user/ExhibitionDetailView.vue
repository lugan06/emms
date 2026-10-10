<template>
  <div class="detail-view">
    <section v-if="loading" class="state-panel">
      <el-skeleton :rows="10" animated />
    </section>
    <el-result v-else-if="error" icon="error" title="展会信息加载失败" sub-title="展会不存在或暂时无法访问">
      <template #extra>
        <el-button type="primary" @click="loadExhibition">重新加载</el-button>
        <el-button @click="goBack">返回列表</el-button>
      </template>
    </el-result>
    <el-empty v-else-if="!exhibition" description="未找到该展会" :image-size="140" />
    <template v-else>
      <section class="detail-hero">
        <el-image v-if="exhibition.coverUrl" :src="exhibition.coverUrl" :alt="exhibition.title" fit="cover" class="detail-cover" />
        <div class="detail-hero-copy">
          <p class="eyebrow">{{ exhibition.exhibitionCode }}</p>
          <h1>{{ exhibition.title }}</h1>
          <p v-if="exhibition.subtitle" class="subtitle">{{ exhibition.subtitle }}</p>
          <div class="hero-meta">
            <span><el-icon><Calendar /></el-icon>{{ formatDateRange(exhibition.startAt, exhibition.endAt) }}</span>
            <span><el-icon><Location /></el-icon>{{ exhibition.venue || '地点待定' }}</span>
          </div>
          <a v-if="exhibition.registrationUrl" class="registration-link" :href="exhibition.registrationUrl" target="_blank" rel="noopener noreferrer">
            立即报名 <el-icon><ArrowRight /></el-icon>
          </a>
        </div>
      </section>

      <section class="detail-section description-section">
        <div class="section-heading">
          <p class="eyebrow">ABOUT THE EXHIBITION</p>
          <h2>展会介绍</h2>
        </div>
        <p class="description">{{ exhibition.description || exhibition.summary || '暂无展会介绍。' }}</p>
      </section>

      <section class="detail-section">
        <div class="section-heading">
          <p class="eyebrow">EXHIBITION DETAILS</p>
          <h2>明细信息</h2>
        </div>
        <dl class="detail-grid">
          <div><dt>展会编码</dt><dd>{{ exhibition.exhibitionCode }}</dd></div>
          <div><dt>举办年份</dt><dd>{{ exhibition.year }}</dd></div>
          <div><dt>届次</dt><dd>{{ exhibition.edition || '暂无' }}</dd></div>
          <div><dt>展馆或地点</dt><dd>{{ exhibition.venue || '暂无' }}</dd></div>
          <div><dt>详细地址</dt><dd>{{ exhibition.address || '暂无' }}</dd></div>
          <div><dt>联系人</dt><dd>{{ exhibition.contactName || '暂无' }}</dd></div>
          <div><dt>联系电话</dt><dd>{{ exhibition.contactPhone || '暂无' }}</dd></div>
          <div><dt>发布时间</dt><dd>{{ formatDateTime(exhibition.publishedAt) }}</dd></div>
        </dl>
      </section>

      <div class="back-row">
        <el-button :icon="ArrowLeft" @click="goBack">返回展会列表</el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight, Calendar, Location } from '@element-plus/icons-vue'

import { publicApi } from '@/api/public'
import type { PublicExhibition } from '@/types/public'
import { formatDateRange, formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const exhibition = ref<PublicExhibition | null>(null)
const loading = ref(true)
const error = ref(false)

async function loadExhibition(): Promise<void> {
  const id = Number(route.params.id)
  if (!Number.isInteger(id) || id <= 0) {
    exhibition.value = null
    error.value = true
    loading.value = false
    return
  }

  loading.value = true
  error.value = false
  try {
    exhibition.value = await publicApi.exhibition(id)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

function goBack(): void {
  void router.push({ name: 'public-exhibitions' })
}

watch(() => route.params.id, () => {
  void loadExhibition()
}, { immediate: true })
</script>

<style scoped>
.detail-view {
  display: grid;
  gap: 24px;
}

.state-panel,
.detail-section {
  padding: 32px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.detail-hero {
  display: grid;
  grid-template-columns: minmax(280px, 0.95fr) minmax(0, 1.25fr);
  min-height: 340px;
  overflow: hidden;
  color: #ffffff;
  background: #1e3a5f;
  border-radius: 8px;
}

.detail-cover {
  width: 100%;
  height: 100%;
  min-height: 340px;
}

.detail-hero-copy {
  align-self: center;
  padding: 48px;
}

.eyebrow {
  margin: 0 0 10px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.detail-hero-copy .eyebrow {
  color: #93c5fd;
}

h1 {
  margin: 0;
  font-size: clamp(30px, 4vw, 48px);
  line-height: 1.15;
}

.subtitle {
  margin: 16px 0 0;
  color: #dbeafe;
  font-size: 17px;
  line-height: 1.6;
}

.hero-meta {
  display: grid;
  gap: 10px;
  margin-top: 26px;
  color: #dbeafe;
  font-size: 14px;
}

.hero-meta span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.registration-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 28px;
  padding: 10px 14px;
  color: #1e3a5f;
  font-size: 13px;
  background: #ffffff;
  border-radius: 4px;
}

.section-heading {
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.section-heading h2 {
  margin: 0;
  color: #111827;
  font-size: 25px;
}

.description {
  margin: 24px 0 0;
  color: #4b5563;
  line-height: 2;
  white-space: pre-line;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin: 24px 0 0;
  border-top: 1px solid #e5e7eb;
  border-left: 1px solid #e5e7eb;
}

.detail-grid div {
  display: grid;
  grid-template-columns: 112px 1fr;
  gap: 16px;
  padding: 16px;
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
}

.detail-grid dt {
  color: #9ca3af;
}

.detail-grid dd {
  min-width: 0;
  margin: 0;
  color: #374151;
  overflow-wrap: anywhere;
}

.back-row {
  display: flex;
  justify-content: flex-start;
}

@media (max-width: 720px) {
  .detail-hero {
    grid-template-columns: 1fr;
  }

  .detail-cover {
    min-height: 220px;
    max-height: 280px;
  }

  .detail-hero-copy,
  .detail-section {
    padding: 24px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
