<template>
  <div class="content-view">
    <section v-if="loading" class="state-panel">
      <el-skeleton :rows="10" animated />
    </section>
    <el-result v-else-if="error" icon="error" title="内容加载失败" sub-title="内容不存在或暂时无法访问">
      <template #extra>
        <el-button type="primary" @click="loadContent">重新加载</el-button>
        <el-button @click="goBack">返回栏目</el-button>
      </template>
    </el-result>
    <el-empty v-else-if="!content" description="未找到该内容" :image-size="140" />
    <article v-else class="article">
      <header class="article-header">
        <p class="eyebrow">{{ content.categoryName }}</p>
        <h1>{{ content.title }}</h1>
        <div class="article-meta">
          <span>{{ formatDateTime(content.publishedAt) }}</span>
          <span v-if="content.author">作者：{{ content.author }}</span>
          <span v-if="content.source">来源：{{ content.source }}</span>
        </div>
      </header>

      <PublicCoverImage v-if="content.coverUrl" :src="content.coverUrl" :alt="content.title" class="article-cover" />
      <p v-if="content.summary" class="article-summary">{{ content.summary }}</p>
      <PublicRichText class="article-body" :html="content.body || '<p>暂无正文内容。</p>'" />

      <div class="back-row">
        <el-button :icon="ArrowLeft" @click="goBack">返回栏目</el-button>
      </div>
    </article>
    <PublicGuestbookForm v-if="!loading && !error && content?.categoryCode === 'contact'" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

import { publicApi } from '@/api/public'
import PublicCoverImage from '@/components/public/PublicCoverImage.vue'
import PublicGuestbookForm from '@/components/public/PublicGuestbookForm.vue'
import PublicRichText from '@/components/public/PublicRichText.vue'
import type { PublicContent } from '@/types/public'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const content = ref<PublicContent | null>(null)
const loading = ref(true)
const error = ref(false)

async function loadContent(): Promise<void> {
  const id = Number(route.params.id)
  if (!Number.isInteger(id) || id <= 0) {
    content.value = null
    error.value = true
    loading.value = false
    return
  }

  loading.value = true
  error.value = false
  try {
    content.value = await publicApi.contentDetail(id)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

function goBack(): void {
  if (content.value?.categoryCode) {
    void router.push({ name: 'public-category-content', params: { code: content.value.categoryCode } })
    return
  }
  void router.push({ name: 'user-home' })
}

watch(() => route.params.id, () => {
  void loadContent()
}, { immediate: true })
</script>

<style scoped>
.content-view {
  display: grid;
  gap: 24px;
}

.state-panel,
.article {
  padding: 36px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.article-header {
  max-width: 820px;
  margin: 0 auto;
  text-align: center;
}

.eyebrow {
  margin: 0 0 10px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.article-header h1 {
  margin: 0;
  color: #111827;
  font-size: clamp(28px, 4vw, 44px);
  line-height: 1.25;
}

.article-meta {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 18px;
  color: #9ca3af;
  font-size: 13px;
}

.article-cover {
  display: block;
  width: min(100%, 900px);
  height: auto;
  min-height: 220px;
  max-height: 420px;
  margin: 32px auto 0;
  border-radius: 6px;
}

.article-summary {
  width: min(100%, 820px);
  margin: 32px auto 0;
  padding: 18px 20px;
  color: #4b5563;
  line-height: 1.8;
  background: #f8fafc;
  border-left: 3px solid #2563eb;
}

.article-body {
  width: min(100%, 820px);
  margin: 32px auto 0;
  line-height: 2;
}

.back-row {
  width: min(100%, 820px);
  margin: 36px auto 0;
}

@media (max-width: 620px) {
  .state-panel,
  .article {
    padding: 24px 18px;
  }
}
</style>
