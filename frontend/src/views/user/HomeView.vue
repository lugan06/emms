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
      <HomeHero :site="home.site" :exhibition="home.currentExhibition" />
      <div v-if="home.currentExhibition" v-reveal>
        <CurrentExhibitionSection :exhibition="home.currentExhibition" />
      </div>
      <div class="content-sections">
        <PublicContentSection
          v-for="section in contentSections"
          :key="section.code"
          :eyebrow="section.eyebrow"
          :title="section.title"
          :items="section.items"
        />
      </div>
      <PublicNewsMarquee />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import HomeHero from '@/components/public/HomeHero.vue'
import CurrentExhibitionSection from '@/components/public/CurrentExhibitionSection.vue'
import PublicContentSection from '@/components/public/PublicContentSection.vue'
import PublicNewsMarquee from '@/components/public/PublicNewsMarquee.vue'
import { publicApi } from '@/api/public'
import { vReveal } from '@/directives/reveal'
import { usePublicStore } from '@/stores/public'
import type { PublicHome } from '@/types/public'

const publicStore = usePublicStore()
const home = ref<PublicHome | null>(null)
const loading = ref(true)
const error = ref(false)

const contentSections = computed(() => {
  if (!home.value) return []
  const value = home.value
  const overview = value.overviewSections?.map(section => ({
    code: section.categoryCode, eyebrow: 'EXHIBITION OVERVIEW', title: section.categoryName, items: section.items,
  })) ?? [
    { code: 'exhibition-intro', eyebrow: 'ABOUT THE EXHIBITION', title: '展会简介', items: value.exhibitionIntro },
    { code: 'exhibition-scope', eyebrow: 'EXHIBITOR PROFILE', title: '参展范围', items: value.exhibitionScope },
  ]
  return [
    ...overview,
    { code: 'events', eyebrow: 'IN PARALLEL', title: '同期活动', items: value.samePeriodActivities },
  ].filter(section => section.items.length > 0)
})

async function loadHome(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    const value = await publicApi.home()
    home.value = value
    publicStore.setSite(value.site, value.company)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => { void loadHome() })
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

.content-sections {
  display: grid;
  gap: 8px;
}
</style>
