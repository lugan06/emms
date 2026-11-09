<template>
  <section v-if="items.length || error" class="news-marquee" aria-label="新闻资讯">
    <div v-reveal class="news-heading">
      <RouterLink class="news-title-link" :to="newsRoute">
        <p class="news-eyebrow">LATEST NEWS</p>
        <h2>新闻资讯</h2>
      </RouterLink>
      <span v-if="!error">累计 {{ total }} 条新闻</span>
    </div>

    <div v-if="error" class="news-error" role="status">新闻暂时无法加载 <el-button link type="primary" @click="load">重试</el-button></div>
    <div v-else class="news-window">
      <div class="news-track" :style="{ '--news-duration': `${marqueeItems.length * 18}s` }">
        <div
          v-for="groupIndex in [0, 1]"
          :key="groupIndex"
          class="news-group"
          :aria-hidden="groupIndex === 1 ? 'true' : undefined"
        >
          <div
            v-for="(item, index) in marqueeItems"
            :key="`${item.id}-${groupIndex}-${index}`"
            class="news-card-reveal"
          >
            <RouterLink
              class="news-card"
              :to="`/content/${item.id}`"
              :tabindex="groupIndex === 1 || index >= items.length ? -1 : undefined"
            >
              <PublicCoverImage :src="item.coverUrl" :alt="item.title" class="news-cover" />
              <div class="news-card-copy">
                <div class="news-card-meta">
                  <span class="news-category">{{ item.categoryName }}</span>
                  <time v-if="item.publishedAt">{{ formatDateTime(item.publishedAt) }}</time>
                </div>
                <h3>{{ item.title }}</h3>
                <p v-if="item.summary" class="news-summary">{{ item.summary }}</p>
              </div>
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, ref } from 'vue'
import { RouterLink } from 'vue-router'

import type { PublicContentList } from '@/types/public'
import { loadAllNews, newsRoute } from '@/utils/news'
import { vReveal } from '@/directives/reveal'
import { formatDateTime } from '@/utils/format'
import PublicCoverImage from './PublicCoverImage.vue'

const items = ref<PublicContentList[]>([])
const total = ref(0)
const error = ref(false)
let disposed = false

async function load(): Promise<void> {
  error.value = false
  try {
    const result = await loadAllNews()
    if (disposed) return
    items.value = result.items
    total.value = result.total
  } catch {
    if (!disposed) error.value = true
  }
}
onMounted(() => { void load() })
onBeforeUnmount(() => { disposed = true })

const marqueeItems = computed(() => {
  const repeatedItems = [...items.value]
  while (repeatedItems.length > 0 && repeatedItems.length < 4) {
    repeatedItems.push(...items.value.slice(0, 4 - repeatedItems.length))
  }
  return repeatedItems
})
</script>

<style scoped>
.news-marquee {
  min-width: 0;
  padding: 24px 0 8px;
}

.news-title-link { text-decoration: none; }
.news-title-link:hover h2 { color: #2563eb; }
.news-title-link:focus-visible { outline: 2px solid #2563eb; outline-offset: 4px; }
.news-error { color: #6b7280; font-size: 14px; }

.news-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.news-eyebrow {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
}

.news-heading h2 {
  margin: 0;
  color: #111827;
  font-size: 24px;
}

.news-heading > span {
  color: #9ca3af;
  font-size: 13px;
}

.news-window {
  overflow: hidden;
  padding: 8px 0 16px;
  -webkit-mask-image: linear-gradient(to right, transparent, #000 5%, #000 95%, transparent);
  mask-image: linear-gradient(to right, transparent, #000 5%, #000 95%, transparent);
}

.news-track,
.news-group {
  display: flex;
  width: max-content;
}

.news-track {
  animation: news-marquee var(--news-duration, 72s) linear infinite;
}

.news-group {
  gap: 16px;
  padding-right: 16px;
}

.news-card-reveal {
  flex: 0 0 auto;
}

.news-card {
  position: relative;
  display: flex;
  width: 360px;
  min-height: 176px;
  box-sizing: border-box;
  cursor: pointer;
  padding: 14px;
  flex: 0 0 360px;
  align-items: stretch;
  gap: 14px;
  color: #1f2937;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 14px rgb(15 23 42 / 5%);
  text-decoration: none;
  transition: border-color 200ms ease, box-shadow 200ms ease, transform 200ms ease;
}

.news-card:hover,
.news-card:focus-visible {
  border-color: #93c5fd;
  box-shadow: 0 10px 24px rgb(37 99 235 / 12%);
  outline: none;
  transform: translateY(-3px);
}

.news-cover {
  --cover-placeholder-color: #64748b;
  --cover-placeholder-size: 14px;
  --cover-placeholder-background: #f1f5f9;
  width: 104px;
  height: 144px;
  flex: 0 0 104px;
  overflow: hidden;
  border-radius: 6px;
}

.news-card-copy {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.news-card-meta {
  display: flex;
  align-items: flex-start;
  gap: 5px;
  flex-direction: column;
  color: #9ca3af;
  font-size: 11px;
}

.news-category {
  max-width: 100%;
  overflow: hidden;
  color: #2563eb;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-card h3 {
  display: -webkit-box;
  margin: 10px 0 0;
  overflow: hidden;
  color: #111827;
  font-size: 16px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.news-summary {
  display: -webkit-box;
  margin: 8px 0 0;
  overflow: hidden;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.news-track:has(.news-card:hover),
.news-track:has(.news-card:focus-visible) {
  animation-play-state: paused;
}

@keyframes news-marquee {
  to { transform: translateX(-50%); }
}

@media (max-width: 620px) {
  .news-card {
    width: min(300px, calc(100vw - 64px));
    flex-basis: min(300px, calc(100vw - 64px));
  }

  .news-cover {
    width: 88px;
    height: 132px;
    flex-basis: 88px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .news-window { overflow-x: auto; }
  .news-track { animation: none; }
  .news-group[aria-hidden="true"] { display: none; }
  .news-card { transition: none; }
}
</style>
