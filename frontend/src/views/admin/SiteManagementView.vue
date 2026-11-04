<template>
  <div class="cms-page">
    <header class="cms-heading">
      <div><h1>站点管理</h1><p>管理站点、绑定域名及启停状态。</p></div>
      <el-button type="primary" data-testid="create-site" :disabled="busy" @click="openCreate">新建站点</el-button>
    </header>
    <section class="cms-panel">
      <form class="site-search" @submit.prevent="search">
        <el-input v-model="keyword" name="siteKeyword" placeholder="搜索站点名称、编码或域名" clearable maxlength="253" />
        <el-select v-model="status" placeholder="全部状态" clearable aria-label="站点状态">
          <el-option label="启用" value="ENABLED" /><el-option label="停用" value="DISABLED" />
        </el-select>
        <el-button native-type="submit" type="primary" :disabled="loading">搜索</el-button>
        <el-button :disabled="loading" @click="resetSearch">重置</el-button>
      </form>
      <el-result v-if="error" icon="error" title="站点加载失败">
        <template #extra><el-button @click="load">重新加载</el-button></template>
      </el-result>
      <template v-else>
        <el-table v-loading="loading" :data="records" row-key="id" empty-text="暂无站点">
          <el-table-column prop="name" label="站点名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="code" label="站点编码" min-width="130" show-overflow-tooltip />
          <el-table-column label="绑定域名" min-width="260">
            <template #default="{ row }">
              <div v-for="domain in row.domains" :key="domain.domain" class="domain-label">
                <span>{{ domain.domain }}</span><el-tag v-if="domain.primary" size="small">主域名</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90"><template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ row.status === 'ENABLED' ? '启用' : '停用' }}</el-tag>
          </template></el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="175" />
          <el-table-column label="操作" width="160" fixed="right"><template #default="{ row }">
            <el-button link type="primary" :disabled="busy" :data-edit-site="row.id" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 'ENABLED' ? 'danger' : 'success'" :disabled="busy" :data-status-site="row.id" @click="toggleStatus(row)">
              {{ row.status === 'ENABLED' ? '停用' : '启用' }}
            </el-button>
          </template></el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
          :disabled="loading" class="site-pagination" @current-change="load" @size-change="search" />
      </template>
    </section>

    <el-dialog v-model="dialogOpen" :title="editingId ? '编辑站点' : '新建站点'" width="min(720px, 94vw)"
      :close-on-click-modal="false" :close-on-press-escape="!saving" :show-close="!saving" destroy-on-close>
      <el-alert v-if="!editingId" title="新站点默认停用，保存后可在列表中启用。" type="info" :closable="false" />
      <el-form label-position="top" :disabled="saving" @submit.prevent="save">
        <el-form-item label="站点名称" required><el-input v-model="form.name" name="siteName" maxlength="200" /></el-form-item>
        <el-form-item label="站点编码" required>
          <el-input v-model="form.code" name="siteCode" maxlength="50" :disabled="Boolean(editingId)" placeholder="例如 expo-2026，创建后不可修改" />
        </el-form-item>
        <el-form-item label="绑定域名" required>
          <div class="domain-editor">
            <p class="domain-help">仅填域名，不含协议、端口或路径；必须选择一个主域名。</p>
            <div v-for="(domain, index) in form.domains" :key="index" class="domain-row">
              <el-input v-model="domain.domain" :name="'siteDomain-' + index" maxlength="253" placeholder="expo.example.com" />
              <label class="primary-choice"><input type="radio" name="site-primary" :checked="domain.primary" :disabled="saving" @change="setPrimary(index)" />主域名</label>
              <el-button :disabled="saving || form.domains.length === 1" :aria-label="'删除域名 ' + (index + 1)" @click="removeDomain(index)">删除</el-button>
            </div>
            <el-button :disabled="saving || form.domains.length >= 50" @click="form.domains.push({ domain: '', primary: false })">添加域名</el-button>
          </div>
        </el-form-item>
        <el-alert v-if="validationError" :title="validationError" type="error" :closable="false" role="alert" />
        <button type="submit" class="hidden-submit" tabindex="-1" aria-hidden="true" />
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="dialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="save-site" @click="save">保存站点</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sitesApi } from '@/api/sites'
import type { ManagedSite, SiteStatus } from '@/types/sites'
import { createManagedSiteForm, normalizeManagedSite, validateManagedSite } from '@/utils/sites'

const records = ref<ManagedSite[]>([])
const keyword = ref('')
const status = ref<SiteStatus | ''>('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const error = ref(false)
const busy = ref(false)
const saving = ref(false)
const dialogOpen = ref(false)
const editingId = ref<number>()
const validationError = ref('')
const form = reactive(createManagedSiteForm())
let loadSequence = 0

async function load(): Promise<void> {
  const sequence = ++loadSequence
  loading.value = true
  error.value = false
  try {
    const result = await sitesApi.page({ keyword: keyword.value.trim() || undefined, status: status.value || undefined,
      page: page.value, pageSize: pageSize.value })
    if (sequence !== loadSequence) return
    records.value = result.records
    total.value = result.total
  } catch { if (sequence === loadSequence) error.value = true }
  finally { if (sequence === loadSequence) loading.value = false }
}
function search(): void { page.value = 1; void load() }
function resetSearch(): void { keyword.value = ''; status.value = ''; search() }
function openCreate(): void {
  editingId.value = undefined
  validationError.value = ''
  Object.assign(form, createManagedSiteForm())
  dialogOpen.value = true
}
function openEdit(row: ManagedSite): void {
  editingId.value = row.id
  validationError.value = ''
  Object.assign(form, { name: row.name, code: row.code, domains: row.domains.map(item => ({ ...item })) })
  dialogOpen.value = true
}
function setPrimary(index: number): void { form.domains.forEach((item, i) => { item.primary = i === index }) }
function removeDomain(index: number): void {
  const wasPrimary = form.domains[index]?.primary
  form.domains.splice(index, 1)
  if (wasPrimary && form.domains.length) setPrimary(0)
}
async function save(): Promise<void> {
  if (saving.value) return
  const valid = validateManagedSite(form)
  validationError.value = valid === true ? '' : valid
  if (valid !== true) return
  saving.value = true
  busy.value = true
  try {
    const payload = normalizeManagedSite(form)
    if (editingId.value) await sitesApi.update(editingId.value, payload)
    else await sitesApi.create(payload)
    ElMessage.success(editingId.value ? '站点已更新' : '站点已创建，当前为停用状态')
    dialogOpen.value = false
    await load()
  } catch { /* Keep the dialog and entered data; HTTP interceptor displays the error. */ }
  finally { saving.value = false; busy.value = false }
}
async function toggleStatus(row: ManagedSite): Promise<void> {
  if (busy.value) return
  const next: SiteStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const action = next === 'ENABLED' ? '启用' : '停用'
  busy.value = true
  try {
    await ElMessageBox.confirm(`确定${action}“${row.name}”吗？${next === 'DISABLED' ? '停用后将阻止公共访问及新登录。' : ''}`,
      `${action}站点`, { type: 'warning', confirmButtonText: action, cancelButtonText: '取消' })
    await sitesApi.status(row.id, next)
    ElMessage.success(`站点已${action}`)
    await load()
  } catch { /* Cancellation or an API error preserves the current list. */ }
  finally { busy.value = false }
}
onMounted(load)
</script>

<style scoped>
@import '@/styles/admin-cms.css';
.site-search { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 20px; }
.site-search .el-input { width: min(320px, 100%); }
.site-search .el-select { width: 140px; }
.domain-label { display: flex; align-items: center; gap: 8px; overflow-wrap: anywhere; }
.site-pagination { margin-top: 20px; overflow-x: auto; }
.domain-editor { width: 100%; display: grid; gap: 12px; }
.domain-help { color: #64748b; margin: 0; font-size: 13px; }
.domain-row { display: flex; align-items: center; gap: 10px; }
.primary-choice { white-space: nowrap; display: flex; align-items: center; gap: 4px; }
.el-alert { margin-bottom: 16px; }
.hidden-submit { display: none; }
@media (max-width: 560px) { .domain-row { flex-wrap: wrap; } .domain-row .el-input { width: 100%; } }
</style>

