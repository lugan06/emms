<template>
  <section class="content-section">
    <div v-reveal class="section-heading">
      <div>
        <p class="eyebrow">{{ eyebrow }}</p>
        <h2>{{ title }}</h2>
      </div>
      <span class="section-count">{{ items.length }} 项</span>
    </div>

    <el-empty v-if="items.length === 0" :description="emptyText" :image-size="72" />
    <div v-else class="content-grid">
      <div
        v-for="(item, index) in items"
        :key="item.id"
        v-reveal="(index % 4) * 70"
      >
        <article class="content-item">
          <PublicCoverImage v-if="item.coverUrl" :src="item.coverUrl" :alt="item.title" class="content-cover" />
          <div class="content-copy">
            <p class="content-date">{{ formatDateTime(item.publishedAt) }}</p>
            <h3>{{ item.title }}</h3>
            <p v-if="item.summary" class="content-summary">{{ item.summary }}</p>
            <PublicRichText v-if="item.body" class="content-body" :html="item.body" />
            <p v-if="item.source" class="content-source">来源：{{ item.source }}</p>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { PublicContent } from '@/types/public'
import { vReveal } from '@/directives/reveal'
import { formatDateTime } from '@/utils/format'
import PublicCoverImage from './PublicCoverImage.vue'
import PublicRichText from './PublicRichText.vue'

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
  border-radius: 16px;
  box-shadow: 0 8px 24px rgb(15 23 42 / 6%);
  transition: border-color 300ms ease, box-shadow 300ms ease, transform 300ms ease;
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
  box-shadow: 0 18px 38px rgb(15 23 42 / 12%);
  transform: translateY(-4px);
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
}

.content-source {
  margin-top: 10px;
}

@media (max-width: 620px) {
  .content-copy { padding: 18px; }
  .content-cover { height: 180px; }
}

@media (prefers-reduced-motion: reduce) {
  .content-item,
  .content-item::before,
  .content-cover,
  h3 {
    transition: none;
  }
}
</style>
