<template>
  <div class="page-view">
    <header class="page-heading">
      <div>
        <p class="eyebrow">EXHIBITION MANAGEMENT</p>
        <h1>展会管理</h1>
        <p>维护展会基础信息、发布状态和首页展示配置。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="createExhibition">新增展会</el-button>
    </header>

    <section class="filter-panel">
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索标题、业务编码或地点"
        class="keyword-input"
        @keyup.enter="submitSearch"
      />
      <el-select v-model="status" clearable placeholder="全部状态" class="status-select">
        <el-option v-for="item in exhibitionStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-input-number v-model="year" :min="2000" :max="2200" :step="1" controls-position="right" placeholder="年份" />
      <el-button type="primary" :icon="Search" @click="submitSearch">查询</el-button>
      <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
    </section>

    <section class="table-panel">
      <el-table v-loading="loading" :data="result.records" row-key="id" empty-text="暂无展会数据">
        <el-table-column prop="id" label="ID" width="72" />
        <el-table-column label="封面图" width="96">
          <template #default="{ row }">
            <el-image v-if="row.coverUrl" :src="resolveAssetUrl(row.coverUrl)" :alt="row.title" fit="cover" class="cover-thumb" />
            <span v-else class="cover-empty">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="exhibitionCode" label="业务编码" width="150" show-overflow-tooltip />
        <el-table-column label="时间" min-width="190">
          <template #default="{ row }">{{ formatDateRange(row.startAt, row.endAt) }}</template>
        </el-table-column>
        <el-table-column prop="venue" label="地点" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><ExhibitionStatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="isActionLoading(row.id)" @click="editExhibition(row.id)">编辑</el-button>
            <el-button
              v-if="row.status === 'DRAFT'"
              link
              type="success"
              :loading="isActionLoading(row.id, 'publish')"
              @click="publishExhibition(row)"
            >发布</el-button>
            <el-button
              v-if="row.status === 'PUBLISHED'"
              link
              type="warning"
              :loading="isActionLoading(row.id, 'offline')"
              @click="offlineExhibition(row)"
            >下架</el-button>
            <el-button
              link
              type="danger"
              :loading="isActionLoading(row.id, 'delete')"
              @click="deleteExhibition(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'

import { adminApi } from '@/api/admin'
import ExhibitionStatusTag from '@/components/admin/ExhibitionStatusTag.vue'
import type { AdminExhibition, AdminExhibitionPage, ExhibitionStatus } from '@/types/admin'
import { resolveAssetUrl } from '@/utils/asset'
import { exhibitionStatusOptions } from '@/utils/exhibition'
import { formatDateRange } from '@/utils/format'

const router = useRouter()
const keyword = ref('')
const status = ref<ExhibitionStatus>()
const year = ref<number>()
const page = ref(1)
const pageSize = 10
const loading = ref(false)
const actionKey = ref<string | null>(null)
const result = ref<AdminExhibitionPage>({ records: [], total: 0, page: 1, pageSize })

async function loadExhibitions(): Promise<void> {
  loading.value = true
  try {
    result.value = await adminApi.exhibitions({
      keyword: keyword.value.trim() || undefined,
      status: status.value,
      year: year.value,
      page: page.value,
      pageSize,
    })
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
  status.value = undefined
  year.value = undefined
  page.value = 1
  void loadExhibitions()
}

function createExhibition(): void {
  void router.push({ name: 'admin-exhibition-create' })
}

function editExhibition(id: number): void {
  void router.push({ name: 'admin-exhibition-edit', params: { id } })
}

function isActionLoading(id: number, action?: string): boolean {
  return actionKey.value === `${action || 'edit'}-${id}`
}

async function publishExhibition(row: AdminExhibition): Promise<void> {
  await confirmAndRun(`确定发布“${row.title}”吗？`, '确认发布', `publish-${row.id}`, () => adminApi.publishExhibition(row.id), '展会已发布')
}

async function offlineExhibition(row: AdminExhibition): Promise<void> {
  await confirmAndRun(`确定下架“${row.title}”吗？`, '确认下架', `offline-${row.id}`, () => adminApi.offlineExhibition(row.id), '展会已下架')
}

async function deleteExhibition(row: AdminExhibition): Promise<void> {
  await confirmAndRun(`删除后“${row.title}”将不再显示，确定继续吗？`, '确认删除', `delete-${row.id}`, () => adminApi.deleteExhibition(row.id), '展会已删除')
}

async function confirmAndRun(
  message: string,
  title: string,
  key: string,
  action: () => Promise<unknown>,
  successMessage: string,
): Promise<void> {
  try {
    await ElMessageBox.confirm(message, title, { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  } catch {
    return
  }

  actionKey.value = key
  try {
    await action()
    ElMessage.success(successMessage)
    if (result.value.records.length === 1 && page.value > 1) {
      page.value -= 1
    }
    await loadExhibitions()
  } finally {
    actionKey.value = null
  }
}

onMounted(() => {
  void loadExhibitions()
})
</script>

<style scoped>
.page-view {
  display: grid;
  gap: 20px;
}

.page-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.page-heading h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
}

.page-heading p:last-child {
  margin: 8px 0 0;
  color: #6b7280;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.filter-panel,
.table-panel {
  padding: 18px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.filter-panel {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.keyword-input {
  flex: 1 1 260px;
  max-width: 380px;
}

.status-select {
  width: 140px;
}

.cover-thumb {
  width: 56px;
  height: 40px;
  border-radius: 3px;
}

.cover-empty {
  color: #9ca3af;
  font-size: 12px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding-top: 18px;
}

@media (max-width: 720px) {
  .page-heading {
    flex-direction: column;
  }

  .filter-panel :deep(.el-input-number) {
    width: 140px;
  }
}
</style>
