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
      <article v-for="item in items" :key="item.id" class="content-item">
        <el-image v-if="item.coverUrl" :src="item.coverUrl" :alt="item.title" fit="cover" class="content-cover" />
        <div class="content-copy">
          <p class="content-date">{{ formatDateTime(item.publishedAt) }}</p>
          <h3>{{ item.title }}</h3>
          <p v-if="item.summary" class="content-summary">{{ item.summary }}</p>
          <p v-if="item.source" class="content-source">来源：{{ item.source }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { PublicContentList } from '@/types/public'
import { formatDateTime } from '@/utils/format'

withDefaults(
  defineProps<{
    eyebrow: string
    title: string
    items: PublicContentList[]
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
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.content-item {
  display: flex;
  min-height: 148px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.content-cover {
  flex: 0 0 120px;
  width: 120px;
  min-height: 148px;
}

.content-copy {
  min-width: 0;
  padding: 16px;
}

.content-date,
.content-source {
  margin: 0;
  color: #9ca3af;
  font-size: 12px;
}

h3 {
  display: -webkit-box;
  overflow: hidden;
  margin: 8px 0 0;
  color: #1f2937;
  font-size: 16px;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.content-summary {
  display: -webkit-box;
  overflow: hidden;
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.content-source {
  margin-top: 10px;
}

@media (max-width: 900px) {
  .content-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
