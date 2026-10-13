<template>
  <div class="cms-page">
    <header class="cms-heading"><div><h1>公司信息</h1><p>维护公司名称、联系方式和企业资料。</p></div><el-button :disabled="saving" @click="load">重新加载</el-button></header>
    <el-alert v-if="!canSave" title="当前账号仅可查看，保存需要 SUPER_ADMIN 权限" type="info" :closable="false" />
    <el-skeleton v-if="loading" :rows="12" animated class="cms-panel" />
    <el-result v-else-if="error" icon="error" title="公司信息加载失败"><template #extra><el-button @click="load">重试</el-button></template></el-result>
    <el-form v-else ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="saving || !canSave" class="cms-panel" @submit.prevent="save">
      <div class="cms-form-grid">
        <el-form-item v-for="field in fields" :key="field.key" :label="field.label" :prop="field.key"><el-input v-model="form[field.key]" :maxlength="field.max" /></el-form-item>
      </div>
      <el-form-item label="微信二维码"><CoverImageUpload v-model="form.wechatImageUrl" :disabled="saving || !canSave" @uploading-change="uploading = $event" /></el-form-item>
      <el-form-item label="其他信息"><el-input v-model="form.otherInfo" type="textarea" :rows="4" /></el-form-item>
      <div class="cms-actions"><el-button type="primary" native-type="submit" :loading="saving" :disabled="uploading || !canSave">保存公司信息</el-button></div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { systemApi } from '@/api/system'
import { createCompanyForm, isSuperAdmin, validateCompanyForm } from '@/utils/system'
import { useUserStore } from '@/stores/user'
import type { CompanySaveRequest } from '@/types/system'
import CoverImageUpload from '@/components/admin/CoverImageUpload.vue'

const fields: Array<{ key: Exclude<keyof CompanySaveRequest, 'wechatImageUrl' | 'otherInfo'>; label: string; max: number }> = [
  { key: 'companyName', label: '公司名称', max: 200 }, { key: 'address', label: '地址', max: 500 },
  { key: 'postalCode', label: '邮编', max: 20 }, { key: 'contactName', label: '联系人', max: 100 },
  { key: 'mobile', label: '手机', max: 30 }, { key: 'telephone', label: '电话', max: 30 },
  { key: 'fax', label: '传真', max: 30 }, { key: 'email', label: '邮箱', max: 150 },
  { key: 'qq', label: 'QQ', max: 50 }, { key: 'businessLicenseNo', label: '营业执照号', max: 100 },
]
const user = useUserStore()
const canSave = computed(() => isSuperAdmin(user.user))
const form = reactive(createCompanyForm())
const formRef = ref<FormInstance>()
const loading = ref(true), error = ref(false), saving = ref(false), uploading = ref(false)
const rules: FormRules<CompanySaveRequest> = {
  companyName: [{ required: true, whitespace: true, message: '请输入公司名称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }],
}
async function load(): Promise<void> {
  loading.value = true; error.value = false
  try {
    const detail = await systemApi.company()
    for (const key of Object.keys(createCompanyForm()) as Array<keyof CompanySaveRequest>) Object.assign(form, { [key]: detail[key] })
  } catch { error.value = true } finally { loading.value = false }
}
async function save(): Promise<void> {
  if (!canSave.value) { ElMessage.warning('无权限'); return }
  if (saving.value || uploading.value || !await formRef.value?.validate().catch(() => false)) return
  const valid = validateCompanyForm(form)
  if (valid !== true) { ElMessage.warning(valid); return }
  saving.value = true
  try { await systemApi.updateCompany({ ...form, companyName: form.companyName.trim(), email: form.email?.trim() || null }); ElMessage.success('公司信息保存成功') }
  catch { /* Preserve form after errors. */ } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
@import '@/styles/admin-cms.css';
</style>
