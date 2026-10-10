<template>
  <div class="list-view">
    <header class="page-heading">
      <div>
        <p class="eyebrow">EXHIBITIONS</p>
        <h1>展会列表</h1>
        <p>浏览已公开的展会信息，按主题和年份快速查找。</p>
      </div>
    </header>

    <section class="filter-bar" aria-label="展会筛选">
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索展会标题、编码或地点"
        class="keyword-input"
        @keyup.enter="submitSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-input-number v-model="year" :min="2000" :max="2200" :step="1" controls-position="right" placeholder="举办年份" />
      <el-button type="primary" :icon="Search" @click="submitSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
    </section>

    <section v-if="loading" class="exhibition-grid">
      <el-skeleton v-for="index in 6" :key="index" animated class="skeleton-card" />
    </section>
    <el-result v-else-if="error" icon="error" title="展会列表加载失败" sub-title="请稍后重试">
      <template #extra><el-button type="primary" @click="loadExhibitions">重新加载</el-button></template>
    </el-result>
    <template v-else>
      <div v-if="result.records.length > 0" class="exhibition-grid">
        <PublicExhibitionCard v-for="item in result.records" :key="item.id" :exhibition="item" />
      </div>
      <el-empty v-else description="暂无符合条件的展会" :image-size="120" />

      <div v-if="result.total > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="result.total"
          background
          layout="total, prev, pager, next"
          @current-change="loadExhibitions"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'

import PublicExhibitionCard from '@/components/public/PublicExhibitionCard.vue'
import { publicApi } from '@/api/public'
import type { PageResult, PublicExhibition } from '@/types/public'

const keyword = ref('')
const year = ref<number>()
const page = ref(1)
const pageSize = 12
const loading = ref(false)
const error = ref(false)
const result = ref<PageResult<PublicExhibition>>({ records: [], total: 0, page: 1, pageSize })

async function loadExhibitions(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    result.value = await publicApi.exhibitions({
      keyword: keyword.value.trim() || undefined,
      year: year.value,
      page: page.value,
      pageSize,
    })
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

function submitSearch(): void {
  page.value = 1
  void loadExhibitions()
}

function resetSearch(): void {
  keyword.value = ''
  year.value = undefined
  page.value = 1
  void loadExhibitions()
}

onMounted(() => {
  void loadExhibitions()
})
</script>

<style scoped>
.list-view {
  display: grid;
  gap: 28px;
}

.page-heading h1 {
  margin: 0;
  color: #111827;
  font-size: 36px;
}

.page-heading p:last-child {
  margin: 12px 0 0;
  color: #6b7280;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.keyword-input {
  flex: 1 1 320px;
  max-width: 520px;
}

.exhibition-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.skeleton-card {
  height: 330px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

@media (max-width: 900px) {
  .exhibition-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .page-heading h1 {
    font-size: 30px;
  }

  .filter-bar {
    flex-wrap: wrap;
  }

  .keyword-input {
    flex-basis: 100%;
    max-width: none;
  }

  .exhibition-grid {
    grid-template-columns: 1fr;
  }
}
</style>
