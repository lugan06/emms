<template>
  <div class="category-view">
    <PublicPageHeading eyebrow="CATEGORY CONTENT" :title="categoryTitle" description="浏览栏目下已发布的展会资讯与内容。" />

    <section v-if="loading" class="content-list">
      <el-skeleton v-for="index in 4" :key="index" animated class="content-skeleton" />
    </section>
    <el-result v-else-if="error" icon="error" title="栏目内容加载失败" sub-title="请稍后重试">
      <template #extra><el-button type="primary" @click="loadContents">重新加载</el-button></template>
    </el-result>
    <template v-else>
      <el-empty v-if="result.records.length === 0" description="该栏目暂无公开内容" :image-size="140" />
      <section v-else class="content-list">
        <article
          v-for="item in result.records"
          :key="item.id"
          class="content-card"
          tabindex="0"
          @click="openContent(item.id)"
          @keydown.enter="openContent(item.id)"
        >
          <PublicCoverImage :src="item.coverUrl" :alt="item.title" class="content-cover" />
          <div class="content-copy">
            <p class="content-date">{{ formatDateTime(item.publishedAt) }}</p>
            <h2>{{ item.title }}</h2>
            <p v-if="item.summary" class="content-summary">{{ item.summary }}</p>
            <div class="content-meta">
              <span v-if="item.author">作者：{{ item.author }}</span>
              <span v-if="item.source">来源：{{ item.source }}</span>
            </div>
          </div>
        </article>
      </section>

      <div v-if="result.total > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="result.total"
          background
          layout="total, prev, pager, next"
          @current-change="loadContents"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { publicApi } from '@/api/public'
import PublicCoverImage from '@/components/public/PublicCoverImage.vue'
import PublicPageHeading from '@/components/public/PublicPageHeading.vue'
import type { PageResult, PublicContentList } from '@/types/public'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const page = ref(1)
const pageSize = 10
const loading = ref(true)
const error = ref(false)
const result = ref<PageResult<PublicContentList>>({ records: [], total: 0, page: 1, pageSize })

const categoryCode = computed(() => String(route.params.code || ''))
const categoryTitle = computed(() => result.value.records[0]?.categoryName || categoryCode.value || '栏目内容')

async function loadContents(): Promise<void> {
  if (!categoryCode.value) {
    error.value = true
    loading.value = false
    return
  }

  loading.value = true
  error.value = false
  try {
    result.value = await publicApi.categoryContents(categoryCode.value, {
      page: page.value,
      pageSize,
    })
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

function openContent(id: number): void {
  void router.push({ name: 'public-content-detail', params: { id } })
}

watch(categoryCode, () => {
  page.value = 1
  void loadContents()
}, { immediate: true })
</script>

<style scoped>
.category-view {
  display: grid;
  gap: 28px;
}

.content-list {
  display: grid;
  gap: 16px;
}

.content-card {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  min-height: 170px;
  overflow: hidden;
  cursor: pointer;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.content-card:hover,
.content-card:focus-visible {
  border-color: #93c5fd;
  box-shadow: 0 10px 24px rgb(15 23 42 / 8%);
  outline: none;
  transform: translateY(-1px);
}

.content-cover {
  width: 100%;
  height: 100%;
  min-height: 170px;
}

.content-copy {
  min-width: 0;
  padding: 24px 28px;
}

.content-date,
.content-meta {
  margin: 0;
  color: #9ca3af;
  font-size: 12px;
}

.content-copy h2 {
  margin: 10px 0 0;
  color: #111827;
  font-size: 22px;
  line-height: 1.4;
}

.content-summary {
  display: -webkit-box;
  overflow: hidden;
  margin: 12px 0 0;
  color: #6b7280;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.content-meta {
  display: flex;
  gap: 18px;
  margin-top: 18px;
}

.content-skeleton {
  height: 170px;
  padding: 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
}

@media (max-width: 620px) {
  .content-card {
    grid-template-columns: 120px minmax(0, 1fr);
  }

  .content-copy {
    padding: 16px;
  }

  .content-copy h2 {
    font-size: 17px;
  }

  .content-meta {
    display: grid;
    gap: 4px;
    margin-top: 12px;
  }
}
</style>
