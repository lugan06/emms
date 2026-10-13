<template>
  <div class="cms-page">
    <header class="cms-heading"><div><h1>站点配置</h1><p>维护站点展示、SEO 和当前主展会。</p></div><el-button :disabled="saving" @click="load">重新加载</el-button></header>
    <el-alert v-if="!canSave" title="当前账号仅可查看，保存需要 SUPER_ADMIN 权限" type="info" :closable="false" />
    <el-skeleton v-if="loading" :rows="12" animated class="cms-panel" />
    <el-result v-else-if="error" icon="error" title="站点配置加载失败"><template #extra><el-button @click="load">重试</el-button></template></el-result>
    <el-form v-else ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="saving || !canSave" class="cms-panel" @submit.prevent="save">
      <div class="cms-form-grid">
        <el-form-item label="站点标题" prop="siteTitle"><el-input v-model="form.siteTitle" maxlength="200" /></el-form-item>
        <el-form-item label="站点副标题"><el-input v-model="form.siteSubtitle" maxlength="200" /></el-form-item>
        <el-form-item label="域名"><el-input v-model="form.domain" maxlength="255" /></el-form-item>
        <el-form-item label="当前主展会" prop="currentExhibitionId">
          <el-select v-model="form.currentExhibitionId" filterable clearable placeholder="请选择已发布展会" class="full-width" @clear="form.currentExhibitionId = null">
            <el-option v-for="item in exhibitions" :key="item.id" :value="item.id" :label="item.title" />
          </el-select>
          <p v-if="unavailableCurrent" class="config-note">当前配置的展会 #{{ form.currentExhibitionId }} 已不在已发布列表中，请重新选择或清空。</p>
        </el-form-item>
        <el-form-item label="ICP 备案号"><el-input v-model="form.icpNumber" maxlength="100" /></el-form-item>
        <el-form-item label="模板编码"><el-input v-model="form.templateCode" maxlength="100" /></el-form-item>
      </div>
      <el-form-item label="站点 Logo"><CoverImageUpload v-model="form.logoUrl" :disabled="saving || !canSave" @uploading-change="uploading = $event" /></el-form-item>
      <el-form-item label="SEO 关键词"><el-input v-model="form.keywords" maxlength="500" /></el-form-item>
      <el-form-item label="SEO 描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      <el-form-item label="统计代码"><el-input v-model="form.statisticsCode" type="textarea" :rows="4" placeholder="仅保存代码，不在管理页面执行" /></el-form-item>
      <el-form-item label="页脚信息"><el-input v-model="form.footerInfo" type="textarea" :rows="3" /></el-form-item>
      <div class="cms-actions"><el-button type="primary" native-type="submit" :loading="saving" :disabled="uploading || !canSave">保存站点配置</el-button></div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { systemApi } from '@/api/system'
import { loadExhibitionOptions } from '@/utils/admin-options'
import { createSiteForm, isSuperAdmin, validateSiteForm } from '@/utils/system'
import { useUserStore } from '@/stores/user'
import type { AdminExhibition } from '@/types/admin'
import type { SiteSaveRequest } from '@/types/system'
import CoverImageUpload from '@/components/admin/CoverImageUpload.vue'

const user = useUserStore()
const canSave = computed(() => isSuperAdmin(user.user))
const form = reactive(createSiteForm())
const formRef = ref<FormInstance>()
const exhibitions = ref<AdminExhibition[]>([])
const loading = ref(true), error = ref(false), saving = ref(false), uploading = ref(false)
const unavailableCurrent = computed(() => form.currentExhibitionId !== null && !exhibitions.value.some(item => item.id === form.currentExhibitionId))
const rules: FormRules<SiteSaveRequest> = {
  siteTitle: [{ required: true, whitespace: true, message: '请输入站点标题', trigger: 'blur' }],
  currentExhibitionId: [{ validator: (_r, _v, callback) => {
    callback(unavailableCurrent.value ? new Error('请选择已发布的展会或清空') : undefined)
  }, trigger: 'change' }],
}
async function load(): Promise<void> {
  loading.value = true; error.value = false
  try {
    const results = await Promise.allSettled([systemApi.site(), loadExhibitionOptions({ status: 'PUBLISHED' })])
    if (results[0].status === 'rejected') throw results[0].reason
    if (results[1].status === 'rejected') throw results[1].reason
    for (const key of Object.keys(createSiteForm()) as Array<keyof SiteSaveRequest>) Object.assign(form, { [key]: results[0].value[key] })
    exhibitions.value = results[1].value
  } catch { error.value = true } finally { loading.value = false }
}
async function save(): Promise<void> {
  if (!canSave.value) { ElMessage.warning('无权限'); return }
  if (saving.value || uploading.value || !await formRef.value?.validate().catch(() => false)) return
  const valid = validateSiteForm(form, exhibitions.value.map(item => item.id))
  if (valid !== true) { ElMessage.warning(valid); return }
  saving.value = true
  try { await systemApi.updateSite({ ...form, siteTitle: form.siteTitle.trim(), currentExhibitionId: form.currentExhibitionId || null }); ElMessage.success('站点配置保存成功') }
  catch { /* Retain values on backend validation or permission failure. */ } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
@import '@/styles/admin-cms.css';
.config-note { margin: 6px 0 0; color: #e6a23c; line-height: 1.5; }
</style>
