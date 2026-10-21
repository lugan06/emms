<template>
  <section class="hero-section">
    <div class="hero-poster" :style="heroPosterStyle" aria-hidden="true"></div>
    <video
      v-if="videoAllowed"
      ref="heroVideo"
      class="hero-video"
      :class="{ 'is-ready': videoReady }"
      autoplay
      :muted="true"
      loop
      playsinline
      preload="metadata"
      :poster="heroCoverUrl || undefined"
      :key="heroVideoSource"
      aria-hidden="true"
      @canplay="handleVideoCanPlay"
      @error="handleVideoError"
    >
      <source :src="heroVideoSource" type="video/mp4" @error="handleVideoError">
    </video>
    <div class="hero-video-overlay" aria-hidden="true"></div>

    <div class="hero-content">
      <div v-reveal class="hero-copy">
        <p class="hero-eyebrow">EEMS EXHIBITION PLATFORM</p>
        <h1>{{ site.siteTitle || '展会管理平台' }}</h1>
        <p class="hero-subtitle">{{ site.siteSubtitle || site.description || '连接品牌、展商与行业伙伴' }}</p>
      </div>
      <div v-if="exhibition" class="hero-exhibition">
        <div v-reveal="100" class="hero-exhibition-copy">
          <p class="hero-eyebrow">CURRENT EXHIBITION</p>
          <p class="hero-exhibition-date">{{ formatDateRange(exhibition.startAt, exhibition.endAt) }}</p>
          <p v-if="exhibition.venue" class="hero-exhibition-venue">{{ exhibition.venue }}</p>
          <RouterLink :to="`/exhibitions/${exhibition.id}`" class="hero-detail-link">查看展会详情 <el-icon><ArrowRight /></el-icon></RouterLink>
        </div>
      </div>
      <div v-else class="hero-empty">当前暂无主展会</div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'

import { useHeroVideo } from '@/composables/useHeroVideo'
import { vReveal } from '@/directives/reveal'
import type { PublicExhibition, PublicSite } from '@/types/public'
import { resolveAssetUrl } from '@/utils/asset'
import { formatDateRange } from '@/utils/format'

const props = defineProps<{ site: PublicSite; exhibition: PublicExhibition | null }>()
const heroCoverUrl = computed(() => resolveAssetUrl(props.exhibition?.coverUrl))
const heroVideoSource = computed(() => resolveAssetUrl(props.site.heroVideoUrl))
const heroPosterStyle = computed(() => heroCoverUrl.value
  ? { backgroundImage: `url(${JSON.stringify(heroCoverUrl.value)})` }
  : {})
const { heroVideo, videoReady, videoAllowed, handleVideoCanPlay, handleVideoError } = useHeroVideo(heroVideoSource)
</script>

<style scoped>
.hero-section {
  position: relative;
  isolation: isolate;
  min-height: 500px;
  padding: 64px;
  overflow: hidden;
  color: #ffffff;
  background: #1e3a5f;
  border-radius: 8px;
}

.hero-poster,
.hero-video,
.hero-video-overlay {
  position: absolute;
  inset: 0;
}

.hero-poster {
  z-index: 0;
  background-color: #1e3a5f;
  background-position: center;
  background-size: cover;
}

.hero-video {
  z-index: 1;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  pointer-events: none;
  transition: opacity 300ms ease;
}

.hero-video.is-ready {
  opacity: 1;
}

.hero-video-overlay {
  z-index: 2;
  background: rgba(0, 0, 0, 0.4);
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 3;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 0.8fr);
  gap: 48px;
  min-height: 368px;
}

.hero-copy {
  align-self: center;
  max-width: 580px;
}

.hero-eyebrow {
  margin: 0 0 12px;
  color: #93c5fd;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.hero-copy h1 {
  max-width: 650px;
  margin: 0;
  font-size: clamp(32px, 4vw, 56px);
  line-height: 1.1;
}

.hero-subtitle {
  max-width: 520px;
  margin: 20px 0 0;
  color: #dbeafe;
  font-size: 17px;
  line-height: 1.7;
}

.hero-detail-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.hero-exhibition {
  display: flex;
  align-self: center;
  justify-self: end;
  width: min(100%, 430px);
  min-height: 250px;
  overflow: hidden;
  background: transparent;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}

.hero-exhibition-copy {
  align-self: center;
  width: 100%;
  min-width: 0;
  padding: 32px;
  text-shadow: 0 1px 8px rgb(0 0 0 / 24%);
}

.hero-exhibition-copy > p:not(.hero-eyebrow) {
  max-width: 100%;
  margin: 14px 0 0;
  color: #dbeafe;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.65;
  overflow-wrap: break-word;
}

.hero-detail-link {
  margin-top: 22px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.5;
}

.hero-empty {
  align-self: center;
  padding: 32px;
  color: #dbeafe;
  border: 1px dashed rgb(255 255 255 / 30%);
  border-radius: 6px;
}

@media (max-width: 860px) {
  .hero-section {
    min-height: 460px;
    padding: 48px;
  }

  .hero-content {
    grid-template-columns: 1fr;
    min-height: 364px;
  }

  .hero-exhibition {
    max-width: 600px;
    justify-self: start;
    min-height: 0;
  }

  .hero-exhibition-copy { padding: 20px 32px; }
}

@media (max-width: 620px) {
  .hero-section {
    min-height: 540px;
    padding: 28px;
  }

  .hero-content { min-height: 0; }
  .hero-exhibition { width: 100%; }
  .hero-exhibition-copy { padding: 12px 0; }
}

@media (prefers-reduced-motion: reduce) {
  .hero-video { transition: none; }
}
</style>
