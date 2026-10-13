<template>
  <div class="cms-page">
    <header class="cms-heading"><div><h1>留言管理</h1><p>查看留言并标记已读、回复或关闭。</p></div></header>
    <section class="cms-panel cms-filters">
      <el-select v-model="status" clearable placeholder="全部状态"><el-option v-for="option in guestbookStatusOptions" :key="option.value" :value="option.value" :label="option.label" /></el-select>
      <el-button type="primary" :disabled="loading || busy" @click="search">查询</el-button><el-button :disabled="loading || busy" @click="reset">重置</el-button>
    </section>
    <el-result v-if="error" icon="error" title="留言加载失败"><template #extra><el-button @click="load">重新加载</el-button></template></el-result>
    <section v-else class="cms-panel">
      <el-table v-loading="loading" :data="result.records" row-key="id" empty-text="暂无符合条件的留言">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="留言人" min-width="110" />
        <el-table-column label="联系方式（已脱敏）" min-width="190"><template #default="{ row }"><div>{{ row.phone || '无电话' }}</div><div>{{ row.email || '无邮箱' }}</div></template></el-table-column>
        <el-table-column prop="companyName" label="公司" min-width="150" show-overflow-tooltip />
        <el-table-column prop="message" label="留言内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ guestbookStatusLabel(row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="提交时间" width="170"><template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作" width="220" fixed="right"><template #default="{ row }">
          <el-button v-if="row.status === 'UNREAD'" link type="primary" :disabled="busy" @click="markRead(row)">标记已读</el-button>
          <el-button link type="primary" :disabled="busy" @click="openReply(row)">{{ row.status === 'CLOSED' ? '查看' : '回复' }}</el-button>
          <el-button v-if="row.status !== 'CLOSED'" link type="warning" :disabled="busy" @click="close(row)">关闭</el-button>
        </template></el-table-column>
      </el-table>
      <div v-if="result.total > 0" class="cms-pagination"><el-pagination v-model:current-page="page" :page-size="pageSize" :total="result.total" background layout="total, prev, pager, next" :disabled="loading || busy" @current-change="load" /></div>
    </section>
    <el-dialog v-model="dialogOpen" :title="selected?.status === 'CLOSED' ? '查看留言' : '回复留言'" width="min(680px, 94vw)" :close-on-click-modal="false" :show-close="!replying" :close-on-press-escape="!replying" @closed="formRef?.clearValidate()">
      <template v-if="selected">
        <p class="message-text">{{ selected.message }}</p>
        <p v-if="selected.repliedAt">上次回复：{{ formatDateTime(selected.repliedAt) }}</p>
        <el-form ref="formRef" :model="replyForm" :rules="replyRules" label-position="top" :disabled="replying || selected.status === 'CLOSED'" @submit.prevent="reply">
          <el-form-item label="回复内容" prop="replyContent"><el-input v-model="replyForm.replyContent" type="textarea" :rows="6" maxlength="2000" show-word-limit /></el-form-item>
        </el-form>
      </template>
      <template #footer><el-button :disabled="replying" @click="dialogOpen = false">取消</el-button><el-button v-if="selected?.status !== 'CLOSED'" type="primary" :loading="replying" @click="reply">提交回复</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type TagProps } from 'element-plus'
import { systemApi } from '@/api/system'
import type { AdminGuestbook, GuestbookPage, GuestbookReplyRequest, GuestbookStatus } from '@/types/system'
import { guestbookStatusLabel, guestbookStatusOptions, validateReply } from '@/utils/system'
import { formatDateTime } from '@/utils/format'

const status = ref<GuestbookStatus>()
const page = ref(1), pageSize = 10
const result = ref<GuestbookPage>({ records: [], total: 0, page: 1, pageSize })
const loading = ref(false), error = ref(false), busy = ref(false), replying = ref(false), dialogOpen = ref(false)
const selected = ref<AdminGuestbook>()
const formRef = ref<FormInstance>()
const replyForm = reactive<GuestbookReplyRequest>({ replyContent: '' })
const replyRules: FormRules<GuestbookReplyRequest> = { replyContent: [{ validator: (_r, value: string, callback) => {
  const valid = validateReply(value); callback(valid === true ? undefined : new Error(valid))
}, trigger: 'blur' }] }
function statusType(value: GuestbookStatus): TagProps['type'] { return { UNREAD: 'warning', READ: 'primary', REPLIED: 'success', CLOSED: 'info' }[value] as TagProps['type'] }
async function load(): Promise<void> {
  loading.value = true; error.value = false
  try {
    result.value = await systemApi.guestbooks({ status: status.value || undefined, page: page.value, pageSize })
    if (!result.value.records.length && page.value > 1) {
      const lastPage = Math.max(1, Math.ceil(result.value.total / pageSize))
      if (lastPage < page.value) { page.value = lastPage; await load() }
    }
  } catch { error.value = true } finally { loading.value = false }
}
function search(): void { page.value = 1; void load() }
function reset(): void { status.value = undefined; search() }
async function markRead(row: AdminGuestbook): Promise<void> {
  if (busy.value) return
  busy.value = true
  try { await systemApi.readGuestbook(row.id); ElMessage.success('已标记为已读'); await load() }
  catch { /* Keep list on business errors. */ } finally { busy.value = false }
}
function openReply(row: AdminGuestbook): void { selected.value = row; replyForm.replyContent = row.replyContent || ''; dialogOpen.value = true }
async function reply(): Promise<void> {
  if (!selected.value || selected.value.status === 'CLOSED' || replying.value || !await formRef.value?.validate().catch(() => false)) return
  replying.value = true; busy.value = true
  try {
    await systemApi.replyGuestbook(selected.value.id, { replyContent: replyForm.replyContent.trim() })
    ElMessage.success('留言回复成功'); dialogOpen.value = false; await load()
  } catch { /* Preserve the reply on request failure. */ } finally { replying.value = false; busy.value = false }
}
async function close(row: AdminGuestbook): Promise<void> {
  try { await ElMessageBox.confirm(`确定关闭“${row.name}”的留言吗？关闭后不能再回复。`, '关闭留言', { type: 'warning', confirmButtonText: '关闭留言', cancelButtonText: '取消' }) } catch { return }
  busy.value = true
  try { await systemApi.closeGuestbook(row.id); ElMessage.success('留言已关闭'); await load() }
  catch { /* Interceptor displays reason. */ } finally { busy.value = false }
}
onMounted(load)
</script>

<style scoped>
@import '@/styles/admin-cms.css';
.message-text { white-space: pre-wrap; overflow-wrap: anywhere; padding: 14px; background: #f8fafc; border-radius: 4px; }
</style>
