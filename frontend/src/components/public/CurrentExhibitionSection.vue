<template>
  <section class="current-section">
    <div class="current-heading">
      <div>
        <p class="eyebrow">FEATURED EXHIBITION</p>
        <h2>正在发生</h2>
      </div>
      <span>{{ exhibition.exhibitionCode }}</span>
    </div>
    <div class="current-content">
      <div v-if="exhibition.coverUrl" class="current-cover-wrap">
        <PublicCoverImage :src="exhibition.coverUrl" :alt="exhibition.title" class="current-cover" />
      </div>
      <div class="current-info">
        <p class="current-summary">{{ exhibition.summary || '欢迎了解本届展会的最新信息。' }}</p>
        <dl class="current-meta">
          <div><dt>时间</dt><dd>{{ formatDateRange(exhibition.startAt, exhibition.endAt) }}</dd></div>
          <div><dt>地点</dt><dd>{{ exhibition.venue || '地点待定' }}</dd></div>
          <div v-if="exhibition.address"><dt>地址</dt><dd>{{ exhibition.address }}</dd></div>
        </dl>
      </div>
      <RouterLink :to="`/exhibitions/${exhibition.id}`" class="outline-link">了解更多 <el-icon><ArrowRight /></el-icon></RouterLink>
    </div>
  </section>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import PublicCoverImage from './PublicCoverImage.vue'
import type { PublicExhibition } from '@/types/public'
import { formatDateRange } from '@/utils/format'

defineProps<{ exhibition: PublicExhibition }>()
</script>

<style scoped>
.current-section {
  position: relative;
  overflow: hidden;
  padding: 32px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgb(15 23 42 / 6%);
  transition: border-color 300ms ease, box-shadow 300ms ease, transform 300ms ease;
}

.current-section::before {
  position: absolute;
  top: 0;
  left: 32px;
  width: 96px;
  height: 3px;
  content: '';
  background: #2563eb;
  transform: scaleX(0.45);
  transform-origin: left center;
  transition: transform 260ms ease;
}

.current-section:hover,
.current-section:focus-within {
  border-color: #93c5fd;
  box-shadow: 0 18px 38px rgb(15 23 42 / 12%);
  transform: translateY(-4px);
}

.current-section:hover::before,
.current-section:focus-within::before {
  transform: scaleX(1);
}

.current-heading,
.current-content {
  display: flex;
  justify-content: space-between;
  gap: 24px;
}

.current-heading {
  align-items: end;
  padding-bottom: 18px;
  border-bottom: 1px solid #e5e7eb;
}

.eyebrow {
  margin: 0 0 6px;
  color: #93c5fd;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.current-heading h2 {
  margin: 0;
  color: #111827;
  font-size: 25px;
}

.current-heading > span {
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.current-content {
  align-items: stretch;
  padding-top: 22px;
}

.current-cover-wrap {
  flex: 0 0 280px;
  min-height: 190px;
  overflow: hidden;
  border-radius: 6px;
  background: #e5e7eb;
}

.current-cover {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 190px;
  transition: transform 420ms cubic-bezier(0.22, 1, 0.36, 1), filter 220ms ease;
}

.current-section:hover .current-cover,
.current-section:focus-within .current-cover {
  filter: saturate(1.05);
  transform: scale(1.025);
}

.current-info {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  justify-content: space-between;
}

.current-summary {
  max-width: 760px;
  margin: 0;
  color: #4b5563;
  line-height: 1.8;
  transition: color 220ms ease;
}

.current-section:hover .current-summary,
.current-section:focus-within .current-summary {
  color: #1f2937;
}

.current-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px 32px;
  margin: 18px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.current-meta div {
  display: flex;
  gap: 8px;
}

.current-meta dt {
  color: #9ca3af;
}

.current-meta dd {
  margin: 0;
}

.outline-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  align-self: center;
  position: relative;
  overflow: hidden;
  flex: 0 0 auto;
  padding: 9px 14px;
  color: #2563eb;
  font-size: 13px;
  border: 1px solid #bfdbfe;
  border-radius: 4px;
  transition: color 180ms ease, background-color 180ms ease, border-color 180ms ease, transform 180ms ease;
}

.outline-link:hover,
.outline-link:focus-visible {
  color: #1d4ed8;
  background: #eff6ff;
  border-color: #60a5fa;
  transform: translateX(3px);
}

@media (max-width: 860px) {
  .current-cover-wrap { flex-basis: 220px; }
}

@media (max-width: 620px) {
  .current-section { padding: 24px; }
  .current-section::before { left: 24px; }
  .current-content {
    display: grid;
    gap: 20px;
  }
  .current-cover-wrap,
  .current-cover { min-height: 160px; }
  .outline-link { justify-self: start; }
}

@media (prefers-reduced-motion: reduce) {
  .current-section,
  .current-section::before,
  .current-summary,
  .current-cover,
  .outline-link {
    transition: none;
  }
}
</style>
