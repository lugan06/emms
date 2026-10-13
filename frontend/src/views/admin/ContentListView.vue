<template>
  <div class="cms-page">
    <header class="cms-heading"><div><h1>内容管理</h1><p>维护栏目文章、页面正文和发布状态。</p></div><el-button type="primary" @click="router.push({ name: 'admin-content-create' })">新增内容</el-button></header>
    <section class="cms-panel cms-filters">
      <el-input v-model="keyword" clearable placeholder="搜索内容标题" @keyup.enter="search" />
      <el-tree-select v-model="categoryId" :data="categories" :props="{ label: 'name', value: 'id', children: 'children' }" node-key="id" check-strictly filterable clearable placeholder="全部栏目" />
      <el-select v-model="exhibitionId" filterable clearable placeholder="全部展会"><el-option v-for="item in exhibitions" :key="item.id" :value="item.id" :label="item.title" /></el-select>
      <el-select v-model="status" clearable placeholder="全部状态"><el-option v-for="item in exhibitionStatusOptions" :key="item.value" :value="item.value" :label="item.label" /></el-select>
      <el-button type="primary" :disabled="loading" @click="search">查询</el-button><el-button :disabled="loading" @click="reset">重置</el-button>
    </section>
    <el-alert v-if="optionsError" title="栏目或展会选项加载失败" type="error" :closable="false"><el-button link type="primary" @click="loadOptions">重试加载选项</el-button></el-alert>
    <el-result v-if="error" icon="error" title="内容列表加载失败"><template #extra><el-button @click="loadContents">重新加载</el-button></template></el-result>
    <section v-else class="cms-panel">
      <el-table v-loading="loading" :data="result.records" row-key="id" empty-text="暂无符合条件的内容">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="230" show-overflow-tooltip />
        <el-table-column label="所属栏目" min-width="140"><template #default="{ row }">{{ categoryNames.get(row.categoryId) || `栏目 #${row.categoryId}` }}</template></el-table-column>
        <el-table-column label="关联展会" min-width="200" show-overflow-tooltip><template #default="{ row }">{{ row.exhibitionId ? exhibitionNames.get(row.exhibitionId) || `展会 #${row.exhibitionId}` : '无' }}</template></el-table-column>
        <el-table-column label="状态" width="95"><template #default="{ row }"><ExhibitionStatusTag :status="row.status" /></template></el-table-column>
        <el-table-column label="置顶" width="75"><template #default="{ row }">{{ row.isTop ? '是' : '否' }}</template></el-table-column>
        <el-table-column label="推荐" width="75"><template #default="{ row }">{{ row.isRecommend ? '是' : '否' }}</template></el-table-column>
        <el-table-column label="操作" fixed="right" width="205"><template #default="{ row }">
          <el-button link type="primary" :disabled="busy" @click="router.push({ name: 'admin-content-edit', params: { id: row.id } })">编辑</el-button>
          <el-button v-if="row.status === 'DRAFT'" link type="success" :disabled="busy" @click="runAction(row, 'publish')">发布</el-button>
          <el-button v-if="row.status === 'PUBLISHED'" link type="warning" :disabled="busy" @click="runAction(row, 'offline')">下架</el-button>
          <el-button link type="danger" :disabled="busy" @click="runAction(row, 'delete')">删除</el-button>
        </template></el-table-column>
      </el-table>
      <div v-if="result.total > 0" class="cms-pagination"><el-pagination v-model:current-page="page" :page-size="pageSize" :total="result.total" background layout="total, prev, pager, next" :disabled="loading" @current-change="loadContents" /></div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cmsApi } from '@/api/cms'
import ExhibitionStatusTag from '@/components/admin/ExhibitionStatusTag.vue'
import type { AdminCategory, AdminContent, AdminContentPage, AdminExhibition, ExhibitionStatus } from '@/types/admin'
import { flattenCategories } from '@/utils/cms'
import { loadExhibitionOptions } from '@/utils/admin-options'
import { exhibitionStatusOptions } from '@/utils/exhibition'

const router = useRouter()
const keyword = ref('')
const categoryId = ref<number>()
const exhibitionId = ref<number>()
const status = ref<ExhibitionStatus>()
const categories = ref<AdminCategory[]>([])
const exhibitions = ref<AdminExhibition[]>([])
const page = ref(1)
const pageSize = 10
const result = ref<AdminContentPage>({ records: [], total: 0, page: 1, pageSize })
const loading = ref(false)
const error = ref(false)
const optionsError = ref(false)
const busy = ref(false)
const categoryNames = computed(() => new Map(flattenCategories(categories.value).map(node => [node.id, node.name])))
const exhibitionNames = computed(() => new Map(exhibitions.value.map(item => [item.id, item.title])))
async function loadOptions(): Promise<void> {
  optionsError.value = false
  const results = await Promise.allSettled([cmsApi.categoryTree(), loadExhibitionOptions()])
  if (results[0].status === 'fulfilled') categories.value = results[0].value
  else optionsError.value = true
  if (results[1].status === 'fulfilled') exhibitions.value = results[1].value
  else optionsError.value = true
}
async function loadContents(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    result.value = await cmsApi.contents({ categoryId: categoryId.value || undefined, exhibitionId: exhibitionId.value || undefined,
      keyword: keyword.value.trim() || undefined, status: status.value || undefined, page: page.value, pageSize })
  } catch { error.value = true } finally { loading.value = false }
}
function search(): void { page.value = 1; void loadContents() }
function reset(): void { keyword.value = ''; categoryId.value = undefined; exhibitionId.value = undefined; status.value = undefined; search() }
async function runAction(row: AdminContent, action: 'publish' | 'offline' | 'delete'): Promise<void> {
  const label = { publish: '发布', offline: '下架', delete: '删除' }[action]
  try { await ElMessageBox.confirm(`确定${label}“${row.title}”吗？`, `确认${label}`, { type: 'warning', confirmButtonText: label, cancelButtonText: '取消' }) } catch { return }
  busy.value = true
  try {
    if (action === 'publish') await cmsApi.publishContent(row.id)
    else if (action === 'offline') await cmsApi.offlineContent(row.id)
    else await cmsApi.deleteContent(row.id)
    ElMessage.success(`内容已${label}`)
    if (action === 'delete' && result.value.records.length === 1 && page.value > 1) page.value -= 1
    await loadContents()
  } catch { /* Interceptor shows backend business errors. */ } finally { busy.value = false }
}
onMounted(() => { void loadOptions(); void loadContents() })
</script>

<style scoped>
@import '@/styles/admin-cms.css';
</style>
