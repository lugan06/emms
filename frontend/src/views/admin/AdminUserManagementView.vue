<template>
  <div class="cms-page">
    <header class="cms-heading">
      <div><h1>管理员管理</h1><p>管理普通管理员、站点授权与账号状态。</p></div>
      <el-button type="primary" data-testid="create-user" :disabled="busy" @click="openCreate">新增管理员</el-button>
    </header>
    <section class="cms-panel">
      <form class="user-search" @submit.prevent="search">
        <el-input v-model="keyword" name="userKeyword" placeholder="搜索用户名或昵称" clearable maxlength="100" />
        <el-select v-model="status" placeholder="全部状态" clearable aria-label="账号状态">
          <el-option label="启用" value="ENABLED" /><el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-select v-model="siteId" placeholder="全部站点" clearable filterable :loading="optionsLoading" :disabled="optionsError" aria-label="筛选授权站点">
          <el-option v-for="site in options" :key="site.id" :value="site.id" :label="siteLabel(site)" />
        </el-select>
        <el-button native-type="submit" type="primary" :disabled="loading">搜索</el-button>
        <el-button :disabled="loading" @click="resetSearch">重置</el-button>
      </form>
      <el-alert v-if="optionsError" title="站点选项加载失败，授权操作暂不可用。" type="error" :closable="false">
        <el-button link type="primary" @click="loadOptions">重试加载站点</el-button>
      </el-alert>
      <el-result v-if="error" icon="error" title="管理员加载失败">
        <template #extra><el-button @click="load">重新加载</el-button></template>
      </el-result>
      <template v-else>
        <el-table v-loading="loading" :data="records" row-key="id" empty-text="暂无普通管理员">
          <el-table-column prop="username" label="用户名" min-width="145" show-overflow-tooltip />
          <el-table-column prop="nickname" label="昵称" min-width="130" show-overflow-tooltip />
          <el-table-column label="授权站点" min-width="240"><template #default="{ row }">
            <div class="site-tags"><el-tag v-for="site in row.sites" :key="site.id" :type="site.status === 'ENABLED' ? 'primary' : 'info'">{{ siteLabel(site) }}</el-tag>
              <span v-if="!row.siteIds.length" class="muted">无授权</span></div>
          </template></el-table-column>
          <el-table-column label="状态" width="85"><template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ row.status === 'ENABLED' ? '启用' : '禁用' }}</el-tag>
          </template></el-table-column>
          <el-table-column label="操作" width="315" fixed="right"><template #default="{ row }">
            <el-button link type="primary" :disabled="busy" :data-edit-user="row.id" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" :disabled="busy" :data-grant-user="row.id" @click="openGrants(row)">分配站点</el-button>
            <el-button link :type="row.status === 'ENABLED' ? 'danger' : 'success'" :disabled="busy" :data-status-user="row.id" @click="toggleStatus(row)">{{ row.status === 'ENABLED' ? '禁用' : '启用' }}</el-button>
            <el-button link type="warning" :disabled="busy" :data-reset-user="row.id" @click="openPassword(row)">重置密码</el-button>
          </template></el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next" :disabled="loading" class="user-pagination" @current-change="load" @size-change="search" />
      </template>
    </section>

    <el-dialog v-model="dialogOpen" :title="dialogTitle" width="min(600px, 94vw)" :close-on-click-modal="false"
      :close-on-press-escape="!saving" :show-close="!saving" destroy-on-close @closed="clearSecrets">
      <el-form label-position="top" :disabled="saving" @submit.prevent="save">
        <el-form-item label="用户名" :required="mode === 'create'">
          <el-input v-model="form.username" name="adminUsername" :disabled="mode !== 'create'" maxlength="50" autocomplete="off" />
        </el-form-item>
        <el-form-item v-if="mode === 'create' || mode === 'edit'" label="昵称" required>
          <el-input v-model="form.nickname" name="adminNickname" maxlength="100" />
        </el-form-item>
        <template v-if="mode === 'create' || mode === 'password'">
          <el-form-item :label="mode === 'create' ? '初始密码' : '新密码'" required>
            <el-input v-model="form.password" name="adminPassword" type="password" show-password autocomplete="new-password" maxlength="72" placeholder="至少 8 字符，UTF-8 不超过 72 字节" />
          </el-form-item>
          <el-form-item label="确认密码" required>
            <el-input v-model="form.confirmPassword" name="adminConfirmPassword" type="password" show-password autocomplete="new-password" maxlength="72" />
          </el-form-item>
        </template>
        <template v-if="mode === 'create' || mode === 'sites'">
          <el-alert v-if="optionsError" title="站点选项加载失败，请重试后保存。" type="error" :closable="false">
            <el-button link type="primary" @click="loadOptions">重试加载站点</el-button>
          </el-alert>
          <el-form-item label="授权站点">
            <SiteGrantSelect v-model="form.siteIds" :options="options" :loading="optionsLoading" :disabled="saving || optionsError" />
          </el-form-item>
          <p class="muted">可同时授权多个站点；留空表示无授权，该账号将无法登录任何站点。</p>
        </template>
        <el-alert v-if="mode === 'sites' || mode === 'password'" title="保存后，该管理员需要重新登录。" type="warning" :closable="false" />
        <el-alert v-if="validationError" :title="validationError" type="error" :closable="false" role="alert" />
        <button type="submit" class="hidden-submit" tabindex="-1" aria-hidden="true" />
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="dialogOpen = false">取消</el-button>
        <el-button type="primary" data-testid="save-user" :loading="saving"
          :disabled="(mode === 'create' || mode === 'sites') && (optionsLoading || optionsError)" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { usersApi } from '@/api/users'
import { sitesApi } from '@/api/sites'
import type { ManagedAdminUser } from '@/types/users'
import type { SiteOption, SiteStatus } from '@/types/sites'
import SiteGrantSelect from '@/components/admin/SiteGrantSelect.vue'
import { adminCreatePayload, createAdminUserForm, validateAdminCreate, validateAdminNickname, validateAdminPassword, validateAdminSites } from '@/utils/users'

const records = ref<ManagedAdminUser[]>([])
const options = ref<SiteOption[]>([])
const keyword = ref('')
const status = ref<SiteStatus | ''>('')
const siteId = ref<number>()
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const optionsLoading = ref(false)
const optionsError = ref(false)
const error = ref(false)
const busy = ref(false)
const saving = ref(false)
const dialogOpen = ref(false)
const editingId = ref<number>()
const mode = ref<'create' | 'edit' | 'sites' | 'password'>('create')
const dialogTitle = computed(() => ({ create: '新增管理员', edit: '编辑管理员', sites: '分配站点', password: '重置密码' })[mode.value])
const form = reactive(createAdminUserForm())
const validationError = ref('')
let sequence = 0
const siteLabel = (site: SiteOption) => site.name + ' (' + site.code + ')' + (site.status === 'DISABLED' ? ' · 停用' : '')

async function load(): Promise<void> {
  const active = ++sequence
  loading.value = true
  error.value = false
  try {
    const result = await usersApi.page({ keyword: keyword.value.trim() || undefined, status: status.value || undefined,
      siteId: siteId.value || undefined, page: page.value, pageSize: pageSize.value })
    if (active !== sequence) return
    records.value = result.records
    total.value = result.total
  } catch { if (active === sequence) error.value = true }
  finally { if (active === sequence) loading.value = false }
}
async function loadOptions(): Promise<void> {
  if (optionsLoading.value) return
  optionsLoading.value = true
  optionsError.value = false
  try { options.value = await sitesApi.options() }
  catch { optionsError.value = true }
  finally { optionsLoading.value = false }
}
function search(): void { page.value = 1; void load() }
function resetSearch(): void { keyword.value = ''; status.value = ''; siteId.value = undefined; search() }
function clearSecrets(): void { form.password = ''; form.confirmPassword = '' }
function openCreate(): void {
  editingId.value = undefined; mode.value = 'create'; validationError.value = ''
  Object.assign(form, createAdminUserForm())
  dialogOpen.value = true
  void loadOptions()
}
function open(row: ManagedAdminUser, next: 'edit' | 'sites' | 'password'): void {
  editingId.value = row.id; mode.value = next; validationError.value = ''
  Object.assign(form, createAdminUserForm(), { username: row.username, nickname: row.nickname, siteIds: [...row.siteIds] })
  dialogOpen.value = true
  if (next === 'sites') void loadOptions()
}
function openEdit(row: ManagedAdminUser): void { open(row, 'edit') }
function openGrants(row: ManagedAdminUser): void { open(row, 'sites') }
function openPassword(row: ManagedAdminUser): void { open(row, 'password') }
async function save(): Promise<void> {
  if (saving.value || ((mode.value === 'create' || mode.value === 'sites') && (optionsLoading.value || optionsError.value))) return
  const valid = mode.value === 'create' ? validateAdminCreate(form, options.value)
    : mode.value === 'edit' ? validateAdminNickname(form.nickname)
    : mode.value === 'sites' ? validateAdminSites(form.siteIds, options.value)
    : validateAdminPassword(form.password, form.confirmPassword)
  validationError.value = valid === true ? '' : valid
  if (valid !== true) return
  saving.value = true; busy.value = true
  try {
    if (mode.value === 'create') await usersApi.create(adminCreatePayload(form))
    else if (mode.value === 'edit') await usersApi.update(editingId.value!, form.nickname.trim())
    else if (mode.value === 'sites') await usersApi.sites(editingId.value!, [...form.siteIds])
    else await usersApi.password(editingId.value!, form.password)
    ElMessage.success(mode.value === 'password' ? '密码已重置，该管理员需重新登录' : '管理员信息已保存')
    clearSecrets()
    dialogOpen.value = false
    await load()
  } catch { /* Preserve the draft for retry; the HTTP interceptor displays errors. */ }
  finally { saving.value = false; busy.value = false }
}
async function toggleStatus(row: ManagedAdminUser): Promise<void> {
  if (busy.value) return
  busy.value = true
  const next: SiteStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const action = next === 'ENABLED' ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}“${row.username}”吗？${next === 'DISABLED' ? '该账号的当前登录将立即失效。' : '该账号需要重新登录。'}`,
      `${action}管理员`, { type: 'warning', confirmButtonText: action, cancelButtonText: '取消' })
    await usersApi.status(row.id, next)
    ElMessage.success(`管理员已${action}`)
    await load()
  } catch { /* Cancellation or failure leaves the current list intact. */ }
  finally { busy.value = false }
}
onMounted(() => { void load(); void loadOptions() })
</script>

<style scoped>
@import '@/styles/admin-cms.css';
.user-search { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 20px; }
.user-search .el-input { width: min(260px, 100%); }
.user-search .el-select { width: 190px; }
.site-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.muted { color: #64748b; font-size: 13px; }
.user-pagination { margin-top: 20px; overflow-x: auto; }
.el-alert { margin-bottom: 16px; }
.hidden-submit { display: none; }
</style>

