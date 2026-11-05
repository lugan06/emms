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
      <el-form-item label="站点 Logo"><CoverImageUpload v-model="form.logoUrl" :disabled="saving || !canSave" @uploading-change="logoUploading = $event" /></el-form-item>
      <el-form-item label="首页宣传视频"><HeroVideoUpload v-model="form.heroVideoUrl" :disabled="saving || !canSave" @uploading-change="heroVideoUploading = $event" /></el-form-item>
      <el-form-item label="SEO 关键词"><el-input v-model="form.keywords" maxlength="500" /></el-form-item>
      <el-form-item label="SEO 描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      <el-form-item label="统计代码"><el-input v-model="form.statisticsCode" type="textarea" :rows="4" placeholder="仅保存代码，不在管理页面执行" /></el-form-item>
      <section class="footer-settings" aria-labelledby="footer-settings-title">
        <h2 id="footer-settings-title">页脚信息</h2>
        <p class="footer-note">以下信息独立用于公共页脚，留空的项目不显示。保存站点配置后生效。</p>
        <div class="cms-form-grid">
          <el-form-item label="主办单位"><el-input v-model="footer.organizer" name="footerOrganizer" maxlength="500" /></el-form-item>
          <el-form-item label="承办单位"><el-input v-model="footer.contractor" name="footerContractor" maxlength="500" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="footer.phone" name="footerPhone" maxlength="100" /></el-form-item>
          <el-form-item label="联系人"><el-input v-model="footer.contactName" name="footerContactName" maxlength="100" /></el-form-item>
          <el-form-item label="邮箱" prop="footerInfo" :rules="footerEmailRules"><el-input v-model="footer.email" name="footerEmail" maxlength="200" /></el-form-item>
          <el-form-item label="版权单位"><el-input v-model="footer.copyrightOwner" name="footerCopyrightOwner" maxlength="500" placeholder="留空使用公司名称" /></el-form-item>
        </div>
        <el-form-item label="补充信息"><el-input v-model="footer.extraText" name="footerExtraText" type="textarea" :rows="2" placeholder="可选，保留其他页脚文字" /></el-form-item>
      </section>
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
import { createFooterInfo, parseFooterInfo, serializeFooterInfo, validateFooterInfo } from '@/utils/footer'
import { usePublicStore } from '@/stores/public'
import { useUserStore } from '@/stores/user'
import type { AdminExhibition } from '@/types/admin'
import type { SiteSaveRequest } from '@/types/system'
import CoverImageUpload from '@/components/admin/CoverImageUpload.vue'
import HeroVideoUpload from '@/components/admin/HeroVideoUpload.vue'

const user = useUserStore()
const canSave = computed(() => isSuperAdmin(user.user))
const form = reactive(createSiteForm())
const footer = reactive(createFooterInfo())
const publicStore = usePublicStore()
const formRef = ref<FormInstance>()
const exhibitions = ref<AdminExhibition[]>([])
const loading = ref(true), error = ref(false), saving = ref(false), logoUploading = ref(false), heroVideoUploading = ref(false)
const uploading = computed(() => logoUploading.value || heroVideoUploading.value)
const unavailableCurrent = computed(() => form.currentExhibitionId !== null && !exhibitions.value.some(item => item.id === form.currentExhibitionId))
const rules: FormRules<SiteSaveRequest> = {
  siteTitle: [{ required: true, whitespace: true, message: '请输入站点标题', trigger: 'blur' }],
  currentExhibitionId: [{ validator: (_r, _v, callback) => {
    callback(unavailableCurrent.value ? new Error('请选择已发布的展会或清空') : undefined)
  }, trigger: 'change' }],
}
const footerEmailRules = [{ validator: (_r: unknown, _v: unknown, callback: (error?: Error) => void) => {
  const valid = validateFooterInfo(footer)
  callback(valid === true ? undefined : new Error(valid))
}, trigger: 'blur' }]
async function load(): Promise<void> {
  loading.value = true; error.value = false
  try {
    const results = await Promise.allSettled([systemApi.site(), loadExhibitionOptions({ status: 'PUBLISHED' })])
    if (results[0].status === 'rejected') throw results[0].reason
    if (results[1].status === 'rejected') throw results[1].reason
    for (const key of Object.keys(createSiteForm()) as Array<keyof SiteSaveRequest>) Object.assign(form, { [key]: results[0].value[key] })
    Object.assign(footer, parseFooterInfo(form.footerInfo))
    exhibitions.value = results[1].value
  } catch { error.value = true } finally { loading.value = false }
}
async function save(): Promise<void> {
  if (!canSave.value) { ElMessage.warning('无权限'); return }
  if (saving.value || uploading.value || !await formRef.value?.validate().catch(() => false)) return
  const valid = validateSiteForm(form, exhibitions.value.map(item => item.id))
  if (valid !== true) { ElMessage.warning(valid); return }
  saving.value = true
  try {
    const footerInfo = serializeFooterInfo(footer)
    await systemApi.updateSite({ ...form, footerInfo, siteTitle: form.siteTitle.trim(), currentExhibitionId: form.currentExhibitionId || null })
    form.footerInfo = footerInfo
    // Invalidate public configuration so navigating back shows the saved footer.
    publicStore.site = null
    ElMessage.success('站点配置保存成功')
  }
  catch { /* Retain values on backend validation or permission failure. */ } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
@import '@/styles/admin-cms.css';
.config-note { margin: 6px 0 0; color: #e6a23c; line-height: 1.5; }
.footer-settings { margin-top: 24px; padding-top: 24px; border-top: 1px solid #e5e7eb; }
.footer-settings h2 { margin: 0 0 8px; font-size: 18px; }
.footer-note { margin: 0 0 20px; color: #6b7280; font-size: 13px; line-height: 1.6; }
</style>
