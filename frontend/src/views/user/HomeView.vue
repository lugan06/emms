<template>
  <div class="home-view">
    <section v-if="loading" class="state-panel">
      <el-skeleton :rows="8" animated />
    </section>

    <el-result v-else-if="error" icon="error" title="首页暂时无法加载" sub-title="请检查后端服务后重试">
      <template #extra>
        <el-button type="primary" @click="loadHome">重新加载</el-button>
      </template>
    </el-result>

    <template v-else-if="home">
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
          aria-hidden="true"
          @canplay="handleVideoCanPlay"
          @error="handleVideoError"
        >
          <source :src="heroVideoSource" type="video/mp4">
        </video>
        <div class="hero-video-overlay" aria-hidden="true"></div>

        <div class="hero-content">
          <div class="hero-copy">
            <p class="hero-eyebrow">EEMS EXHIBITION PLATFORM</p>
            <h1>{{ home.site.siteTitle || '展会管理平台' }}</h1>
            <p class="hero-subtitle">{{ home.site.siteSubtitle || home.site.description || '连接品牌、展商与行业伙伴' }}</p>
            <RouterLink class="hero-action" to="/exhibitions">浏览全部展会 <el-icon><ArrowRight /></el-icon></RouterLink>
          </div>
          <div v-if="home.currentExhibition" class="hero-exhibition">
            <div v-if="home.currentExhibition.coverUrl" class="hero-image-wrap">
              <el-image :src="resolveAssetUrl(home.currentExhibition.coverUrl)" :alt="home.currentExhibition.title" fit="cover" class="hero-image">
                <template #error><div class="hero-image-fallback">EEMS</div></template>
              </el-image>
            </div>
            <div class="hero-exhibition-copy">
              <p class="hero-eyebrow">CURRENT EXHIBITION</p>
              <h2>{{ home.currentExhibition.title }}</h2>
              <p>{{ formatDateRange(home.currentExhibition.startAt, home.currentExhibition.endAt) }}</p>
              <p v-if="home.currentExhibition.venue">{{ home.currentExhibition.venue }}</p>
              <RouterLink :to="`/exhibitions/${home.currentExhibition.id}`" class="hero-detail-link">查看展会详情 <el-icon><ArrowRight /></el-icon></RouterLink>
            </div>
          </div>
          <div v-else class="hero-empty">当前暂无主展会</div>
        </div>
      </section>

      <section v-if="home.currentExhibition" class="current-section">
        <div class="current-heading">
          <div>
            <p class="eyebrow">FEATURED EXHIBITION</p>
            <h2>正在发生</h2>
          </div>
          <span>{{ home.currentExhibition.exhibitionCode }}</span>
        </div>
        <div class="current-content">
          <div>
            <p class="current-summary">{{ home.currentExhibition.summary || '欢迎了解本届展会的最新信息。' }}</p>
            <dl class="current-meta">
              <div><dt>时间</dt><dd>{{ formatDateRange(home.currentExhibition.startAt, home.currentExhibition.endAt) }}</dd></div>
              <div><dt>地点</dt><dd>{{ home.currentExhibition.venue || '地点待定' }}</dd></div>
              <div v-if="home.currentExhibition.address"><dt>地址</dt><dd>{{ home.currentExhibition.address }}</dd></div>
            </dl>
          </div>
          <RouterLink :to="`/exhibitions/${home.currentExhibition.id}`" class="outline-link">了解更多 <el-icon><ArrowRight /></el-icon></RouterLink>
        </div>
      </section>

      <div class="content-sections">
        <PublicContentSection
          v-for="section in contentSections"
          :key="section.title"
          :eyebrow="section.eyebrow"
          :title="section.title"
          :items="section.items"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'

import PublicContentSection from '@/components/public/PublicContentSection.vue'
import { publicApi } from '@/api/public'
import { usePublicStore } from '@/stores/public'
import type { PublicHome } from '@/types/public'
import { resolveAssetUrl } from '@/utils/asset'
import { formatDateRange } from '@/utils/format'

const publicStore = usePublicStore()
const home = ref<PublicHome | null>(null)
const loading = ref(true)
const error = ref(false)
const heroVideo = ref<HTMLVideoElement | null>(null)
const videoReady = ref(false)
const videoFailed = ref(false)
const prefersStaticHero = ref(false)
const heroVideoSource = '/videos/zhidonghua-b.mp4'
let heroMediaQueries: MediaQueryList[] = []

const heroCoverUrl = computed(() => {
  const coverUrl = home.value?.currentExhibition?.coverUrl
  return coverUrl ? resolveAssetUrl(coverUrl) : ''
})

const heroPosterStyle = computed(() => {
  if (!heroCoverUrl.value) return {}
  const safeUrl = heroCoverUrl.value.replace(/["\\]/g, '\\$&')
  return { backgroundImage: `url("${safeUrl}")` }
})

const videoAllowed = computed(() => !prefersStaticHero.value && !videoFailed.value)

const contentSections = computed(() => {
  if (!home.value) return []
  const value = home.value
  return [
    { eyebrow: 'ABOUT THE EXHIBITION', title: '展会简介', items: value.exhibitionIntro },
    { eyebrow: 'IN PARALLEL', title: '同期活动', items: value.samePeriodActivities },
    { eyebrow: 'EXHIBITOR PROFILE', title: '参展范围', items: value.exhibitionScope },
    { eyebrow: 'EXHIBITION NEWS', title: '展会动态', items: value.exhibitionNews },
    { eyebrow: 'INDUSTRY NEWS', title: '行业新闻', items: value.industryNews },
    { eyebrow: 'EXHIBITOR NEWS', title: '展商新闻', items: value.exhibitorNews },
  ].filter(section => section.items.length > 0)
})

async function loadHome(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    const value = await publicApi.home()
    home.value = value
    videoReady.value = false
    videoFailed.value = false
    publicStore.setSite(value.site, value.company)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

interface NetworkInformationLike {
  saveData?: boolean
  effectiveType?: string
}

function updateHeroPlaybackPreference(): void {
  if (typeof window === 'undefined') return

  const reducedMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches ?? false
  const mobileViewport = window.matchMedia?.('(max-width: 760px)').matches ?? window.innerWidth <= 760
  const connection = (navigator as Navigator & { connection?: NetworkInformationLike }).connection
  const limitedConnection = connection?.saveData === true
    || connection?.effectiveType === 'slow-2g'
    || connection?.effectiveType === '2g'

  prefersStaticHero.value = reducedMotion || mobileViewport || limitedConnection
  if (prefersStaticHero.value) {
    videoReady.value = false
    heroVideo.value?.pause()
  }
}

function handleHeroPreferenceChange(): void {
  updateHeroPlaybackPreference()
}

function handleVideoCanPlay(): void {
  videoFailed.value = false
  videoReady.value = true
}

function handleVideoError(): void {
  videoReady.value = false
  videoFailed.value = true
}

onMounted(() => {
  updateHeroPlaybackPreference()
  if (typeof window.matchMedia === 'function') {
    heroMediaQueries = [
      window.matchMedia('(prefers-reduced-motion: reduce)'),
      window.matchMedia('(max-width: 760px)'),
    ]
    heroMediaQueries.forEach(query => {
      if (typeof query.addEventListener === 'function') {
        query.addEventListener('change', handleHeroPreferenceChange)
      } else {
        query.addListener(handleHeroPreferenceChange)
      }
    })
  }
  window.addEventListener('resize', updateHeroPlaybackPreference)
  void loadHome()
})

onBeforeUnmount(() => {
  heroMediaQueries.forEach(query => {
    if (typeof query.removeEventListener === 'function') {
      query.removeEventListener('change', handleHeroPreferenceChange)
    } else {
      query.removeListener(handleHeroPreferenceChange)
    }
  })
  window.removeEventListener('resize', updateHeroPlaybackPreference)
})
</script>

<style scoped>
.home-view {
  display: grid;
  gap: 32px;
}

.state-panel {
  padding: 40px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

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

.hero-eyebrow,
.eyebrow {
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

.hero-action,
.hero-detail-link,
.outline-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.hero-action {
  margin-top: 28px;
  padding: 11px 16px;
  color: #1e3a5f;
  font-size: 14px;
  background: #ffffff;
  border-radius: 4px;
}

.hero-exhibition {
  display: flex;
  align-self: center;
  min-height: 250px;
  overflow: hidden;
  background: #294a70;
  border: 1px solid rgb(255 255 255 / 20%);
  border-radius: 6px;
}

.hero-image-wrap {
  flex: 0 0 42%;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-image-fallback {
  display: grid;
  width: 100%;
  height: 100%;
  min-height: 250px;
  color: #ffffff;
  font-size: 28px;
  font-weight: 700;
  background: #334155;
  place-items: center;
}

.hero-exhibition-copy {
  align-self: center;
  padding: 28px;
}

.hero-exhibition-copy h2 {
  margin: 0;
  font-size: 23px;
  line-height: 1.35;
}

.hero-exhibition-copy > p:not(.hero-eyebrow) {
  margin: 10px 0 0;
  color: #dbeafe;
  font-size: 13px;
  line-height: 1.5;
}

.hero-detail-link {
  margin-top: 18px;
  color: #ffffff;
  font-size: 13px;
}

.hero-empty {
  align-self: center;
  padding: 32px;
  color: #dbeafe;
  border: 1px dashed rgb(255 255 255 / 30%);
  border-radius: 6px;
}

.current-section {
  position: relative;
  overflow: hidden;
  padding: 32px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgb(15 23 42 / 3%);
  transition: border-color 220ms ease, box-shadow 220ms ease, transform 220ms ease;
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
  box-shadow: 0 16px 32px rgb(37 99 235 / 10%);
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

.current-heading .eyebrow {
  margin-bottom: 6px;
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
  align-items: end;
  padding-top: 22px;
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

.content-sections {
  display: grid;
  gap: 8px;
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
  }
}

@media (max-width: 620px) {
  .hero-section {
    min-height: 540px;
    padding: 28px;
  }

  .current-section {
    padding: 24px;
  }

  .current-section::before {
    left: 24px;
  }

  .hero-content {
    min-height: 0;
  }

  .hero-section,
  .current-section {
    border-radius: 8px;
  }

  .hero-exhibition {
    display: block;
  }

  .hero-image-wrap {
    height: 150px;
  }

  .current-content {
    display: grid;
  }
}

@media (prefers-reduced-motion: reduce) {
  .current-section,
  .current-section::before,
  .current-summary,
  .outline-link {
    transition: none;
  }
}
</style>
