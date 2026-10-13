<template>
  <div class="cms-page">
    <header class="cms-heading"><div><h1>日志管理</h1><p>查询已脱敏的操作审计记录。</p></div></header>
    <el-result v-if="!canView" icon="warning" title="无权限" sub-title="仅超级管理员可查看日志" />
    <template v-else>
      <section class="cms-panel cms-filters">
        <el-input v-model="username" clearable placeholder="操作人账号（精确匹配）" @keyup.enter="search" />
        <el-input v-model="module" clearable placeholder="业务模块" @keyup.enter="search" />
        <el-input v-model="operation" clearable placeholder="操作（模糊匹配）" @keyup.enter="search" />
        <el-select v-model="operationResult" clearable placeholder="全部结果"><el-option label="成功" value="SUCCESS" /><el-option label="失败" value="FAILURE" /></el-select>
        <el-date-picker v-model="startAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="开始时间" />
        <el-date-picker v-model="endAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="结束时间" />
        <el-button type="primary" :disabled="loading" @click="search">查询</el-button><el-button :disabled="loading" @click="reset">重置</el-button>
      </section>
      <el-result v-if="error" icon="error" title="日志加载失败"><template #extra><el-button @click="load">重新加载</el-button></template></el-result>
      <section v-else class="cms-panel">
        <el-table v-loading="loading" :data="result.records" row-key="id" empty-text="暂无符合条件的操作日志">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="操作人" min-width="110" />
          <el-table-column prop="module" label="模块" min-width="130" />
          <el-table-column prop="operation" label="操作" min-width="150" show-overflow-tooltip />
          <el-table-column prop="requestMethod" label="请求方法" width="100" />
          <el-table-column prop="requestUrl" label="请求 URL" min-width="240" show-overflow-tooltip />
          <el-table-column prop="requestIp" label="请求 IP" min-width="130" />
          <el-table-column label="结果" width="85"><template #default="{ row }"><el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'">{{ row.result === 'SUCCESS' ? '成功' : '失败' }}</el-tag></template></el-table-column>
          <el-table-column label="时间" width="170"><template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template></el-table-column>
          <el-table-column label="详情" width="80" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="selected = row">查看</el-button></template></el-table-column>
        </el-table>
        <div v-if="result.total > 0" class="cms-pagination"><el-pagination v-model:current-page="page" :page-size="pageSize" :total="result.total" background layout="total, prev, pager, next" :disabled="loading" @current-change="load" /></div>
      </section>
    </template>
    <el-dialog :model-value="Boolean(selected)" title="日志详情（已脱敏）" width="min(720px, 94vw)" @close="selected = undefined">
      <p>请求参数</p><pre>{{ selected?.requestParams || '无' }}</pre>
      <p>失败原因</p><pre>{{ selected?.errorMessage || '无' }}</pre>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { systemApi } from '@/api/system'
import { isSuperAdmin, validateLogRange } from '@/utils/system'
import { formatDateTime } from '@/utils/format'
import type { LogPage, LogQuery, OperationLog, OperationResult } from '@/types/system'

const user = useUserStore()
const canView = computed(() => isSuperAdmin(user.user))
const username = ref(''), module = ref(''), operation = ref('')
const operationResult = ref<OperationResult>()
const startAt = ref<string | null>(null), endAt = ref<string | null>(null)
const page = ref(1), pageSize = 20
const result = ref<LogPage>({ records: [], total: 0, page: 1, pageSize })
const loading = ref(false), error = ref(false)
const selected = ref<OperationLog>()
function query(): LogQuery {
  return { username: username.value.trim() || undefined, module: module.value.trim() || undefined,
    operation: operation.value.trim() || undefined, result: operationResult.value || undefined,
    startAt: startAt.value || undefined, endAt: endAt.value || undefined, page: page.value, pageSize }
}
async function load(): Promise<void> {
  if (!canView.value) return
  const params = query(), valid = validateLogRange(params)
  if (valid !== true) { ElMessage.warning(valid); return }
  loading.value = true; error.value = false
  try { result.value = await systemApi.logs(params) }
  catch { error.value = true } finally { loading.value = false }
}
function search(): void {
  const valid = validateLogRange(query())
  if (valid !== true) { ElMessage.warning(valid); return }
  page.value = 1; void load()
}
function reset(): void { username.value = ''; module.value = ''; operation.value = ''; operationResult.value = undefined; startAt.value = null; endAt.value = null; search() }
onMounted(load)
</script>

<style scoped>
@import '@/styles/admin-cms.css';
pre { white-space: pre-wrap; overflow-wrap: anywhere; padding: 14px; background: #f8fafc; border-radius: 4px; }
</style>
