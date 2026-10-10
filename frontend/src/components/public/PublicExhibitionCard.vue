<template>
  <article class="exhibition-card">
    <RouterLink :to="`/exhibitions/${exhibition.id}`" class="card-link">
      <div class="cover-wrap">
        <el-image
          v-if="exhibition.coverUrl"
          :src="exhibition.coverUrl"
          :alt="exhibition.title"
          fit="cover"
          lazy
          class="cover"
        />
        <div v-else class="cover-fallback">EEMS</div>
        <span v-if="exhibition.isCurrent" class="current-badge">当前展会</span>
      </div>
      <div class="card-body">
        <p class="card-code">{{ exhibition.exhibitionCode }}</p>
        <h3>{{ exhibition.title }}</h3>
        <p v-if="exhibition.subtitle" class="subtitle">{{ exhibition.subtitle }}</p>
        <dl class="card-meta">
          <div>
            <dt>时间</dt>
            <dd>{{ formatDateRange(exhibition.startAt, exhibition.endAt) }}</dd>
          </div>
          <div v-if="exhibition.venue">
            <dt>地点</dt>
            <dd>{{ exhibition.venue }}</dd>
          </div>
        </dl>
      </div>
    </RouterLink>
  </article>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'

import type { PublicExhibition } from '@/types/public'
import { formatDateRange } from '@/utils/format'

defineProps<{
  exhibition: PublicExhibition
}>()
</script>

<style scoped>
.exhibition-card {
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.exhibition-card:hover {
  border-color: #c7d2fe;
  box-shadow: 0 12px 28px rgb(15 23 42 / 8%);
  transform: translateY(-2px);
}

.card-link {
  display: block;
  height: 100%;
}

.cover-wrap {
  position: relative;
  aspect-ratio: 16 / 8;
  overflow: hidden;
  background: #e5e7eb;
}

.cover {
  width: 100%;
  height: 100%;
}

.cover-fallback {
  display: grid;
  width: 100%;
  height: 100%;
  color: #ffffff;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.08em;
  background: #334155;
  place-items: center;
}

.current-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 8px;
  color: #ffffff;
  font-size: 12px;
  background: #2563eb;
  border-radius: 3px;
}

.card-body {
  padding: 20px;
}

.card-code {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

h3 {
  margin: 0;
  color: #111827;
  font-size: 19px;
  line-height: 1.4;
}

.subtitle {
  display: -webkit-box;
  overflow: hidden;
  margin: 8px 0 0;
  color: #6b7280;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.card-meta {
  display: grid;
  gap: 8px;
  margin: 18px 0 0;
  color: #4b5563;
  font-size: 13px;
}

.card-meta div {
  display: grid;
  grid-template-columns: 36px 1fr;
  gap: 8px;
}

dt {
  color: #9ca3af;
}

dd {
  margin: 0;
}
</style>
