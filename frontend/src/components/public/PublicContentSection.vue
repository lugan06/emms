<template>
  <section class="content-section">
    <div class="section-heading">
      <div>
        <p class="eyebrow">{{ eyebrow }}</p>
        <h2>{{ title }}</h2>
      </div>
      <span class="section-count">{{ items.length }} 项</span>
    </div>

    <el-empty v-if="items.length === 0" :description="emptyText" :image-size="72" />
    <div v-else class="content-grid">
      <article
        v-for="(item, index) in items"
        :key="item.id"
        class="content-item"
        :style="{ animationDelay: `${index * 80}ms` }"
      >
        <el-image v-if="item.coverUrl" :src="resolveAssetUrl(item.coverUrl)" :alt="item.title" fit="cover" class="content-cover">
          <template #error><div class="content-cover-fallback">EEMS</div></template>
        </el-image>
        <div class="content-copy">
          <p class="content-date">{{ formatDateTime(item.publishedAt) }}</p>
          <h3>{{ item.title }}</h3>
          <p v-if="item.summary" class="content-summary">{{ item.summary }}</p>
          <div v-if="item.body" class="content-body" v-html="item.body" />
          <p v-if="item.source" class="content-source">来源：{{ item.source }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { PublicContent } from '@/types/public'
import { resolveAssetUrl } from '@/utils/asset'
import { formatDateTime } from '@/utils/format'

withDefaults(
  defineProps<{
    eyebrow: string
    title: string
    items: PublicContent[]
    emptyText?: string
  }>(),
  {
    emptyText: '暂无内容',
  },
)
</script>

<style scoped>
.content-section {
  padding: 32px 0;
}

.section-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
}

h2 {
  margin: 0;
  color: #111827;
  font-size: 24px;
}

.section-count {
  color: #9ca3af;
  font-size: 13px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 16px;
}

.content-item {
  position: relative;
  display: block;
  min-height: 148px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgb(15 23 42 / 3%);
  animation: content-card-in 560ms cubic-bezier(0.22, 1, 0.36, 1) both;
  transition: border-color 220ms ease, box-shadow 220ms ease, transform 220ms ease;
}

.content-item::before {
  position: absolute;
  inset: 0 auto 0 0;
  z-index: 1;
  width: 3px;
  content: '';
  background: #2563eb;
  opacity: 0;
  transform: scaleY(0.35);
  transform-origin: center;
  transition: opacity 220ms ease, transform 220ms ease;
}

.content-item:hover,
.content-item:focus-within {
  border-color: #93c5fd;
  box-shadow: 0 14px 30px rgb(37 99 235 / 10%);
  transform: translateY(-5px);
}

.content-item:hover::before,
.content-item:focus-within::before {
  opacity: 1;
  transform: scaleY(1);
}

.content-cover {
  display: block;
  width: 100%;
  height: 280px;
  transition: transform 420ms cubic-bezier(0.22, 1, 0.36, 1), filter 220ms ease;
}

.content-item:hover .content-cover,
.content-item:focus-within .content-cover {
  filter: saturate(1.05);
  transform: scale(1.025);
}

.content-cover-fallback {
  display: grid;
  width: 100%;
  height: 100%;
  min-height: 148px;
  color: #ffffff;
  font-size: 22px;
  font-weight: 700;
  background: #334155;
  place-items: center;
}

.content-copy {
  min-width: 0;
  padding: 24px;
}

.content-date,
.content-source {
  margin: 0;
  color: #9ca3af;
  font-size: 12px;
}

h3 {
  margin: 8px 0 0;
  color: #1f2937;
  font-size: 16px;
  line-height: 1.45;
  overflow-wrap: anywhere;
  transition: color 180ms ease;
}

.content-item:hover h3,
.content-item:focus-within h3 {
  color: #1d4ed8;
}

.content-summary {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.content-body {
  margin-top: 20px;
  color: #374151;
  font-size: 16px;
  line-height: 1.9;
  overflow-wrap: anywhere;
}

.content-body :deep(p) { margin: 0 0 16px; }
.content-body :deep(p:last-child) { margin-bottom: 0; }
.content-body :deep(img) { max-width: 100%; height: auto; }
.content-body :deep(h2),
.content-body :deep(h3) { margin: 24px 0 12px; line-height: 1.4; }
.content-body :deep(ul),
.content-body :deep(ol) { padding-left: 24px; }
.content-body :deep(pre) { white-space: pre-wrap; }
.content-body :deep(table) { display: block; max-width: 100%; overflow-x: auto; border-collapse: collapse; }
.content-body :deep(td),
.content-body :deep(th) { padding: 8px 12px; border: 1px solid #e5e7eb; }

.content-source {
  margin-top: 10px;
}

@media (max-width: 620px) {
  .content-copy { padding: 18px; }
  .content-cover { height: 180px; }
}

@keyframes content-card-in {
  from {
    opacity: 0;
    transform: translateY(14px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .content-item,
  .content-item::before,
  .content-cover,
  h3 {
    animation: none;
    transition: none;
  }
}
</style>
