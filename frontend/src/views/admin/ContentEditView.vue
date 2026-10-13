<template>
  <div class="cms-page">
    <header class="cms-heading"><div><h1>{{ editingId ? '编辑内容' : '新增内容' }}</h1><p>维护所属栏目、正文、封面与展示配置。</p></div><el-button @click="goBack">返回列表</el-button></header>
    <el-skeleton v-if="loading" :rows="14" animated class="cms-panel" />
    <el-result v-else-if="error" icon="error" title="内容或表单选项加载失败"><template #extra><el-button type="primary" @click="loadForm">重新加载</el-button></template></el-result>
    <el-form v-else ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="saving" @submit.prevent="save">
      <section class="cms-panel">
        <h2 class="cms-section-title">基础信息</h2>
        <div class="cms-form-grid">
          <el-form-item label="所属栏目" prop="categoryId">
            <el-tree-select v-model="form.categoryId" :data="categories" :props="{ label: 'name', value: 'id', children: 'children' }" node-key="id" check-strictly filterable placeholder="请选择所属栏目" class="full-width" />
          </el-form-item>
          <el-form-item label="关联展会" prop="exhibitionId"><el-select v-model="form.exhibitionId" filterable clearable placeholder="不关联展会" class="full-width"><el-option v-for="item in exhibitions" :key="item.id" :label="item.title" :value="item.id" /></el-select></el-form-item>
          <el-form-item label="标题" prop="title"><el-input v-model="form.title" maxlength="200" show-word-limit /></el-form-item>
          <el-form-item label="URL 标识 slug"><el-input v-model="form.slug" maxlength="150" /></el-form-item>
          <el-form-item label="作者"><el-input v-model="form.author" maxlength="100" /></el-form-item>
          <el-form-item label="来源"><el-input v-model="form.source" maxlength="200" /></el-form-item>
        </div>
        <el-form-item label="封面图"><CoverImageUpload v-model="form.coverUrl" :disabled="saving" @uploading-change="coverUploading = $event" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" :rows="3" maxlength="2000" show-word-limit /></el-form-item>
        <el-form-item label="正文"><ContentRichEditor v-model="form.body" @uploading-change="bodyUploading = $event" /></el-form-item>
        <div class="cms-form-grid">
          <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="form.sortOrder" :min="0" :precision="0" /></el-form-item>
          <el-form-item label="置顶"><el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-form-item label="推荐"><el-switch v-model="form.isRecommend" :active-value="1" :inactive-value="0" /></el-form-item>
        </div>
        <el-form-item label="扩展属性 JSON" prop="extraData"><el-input v-model="form.extraData" type="textarea" :rows="3" placeholder="可选 JSON" /></el-form-item>
        <div class="cms-actions"><el-button :disabled="saving" @click="goBack">取消</el-button><el-button type="primary" native-type="submit" :loading="saving" :disabled="coverUploading || bodyUploading">保存内容</el-button></div>
      </section>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { cmsApi } from '@/api/cms'
import CoverImageUpload from '@/components/admin/CoverImageUpload.vue'
import ContentRichEditor from '@/components/admin/ContentRichEditor.vue'
import type { AdminCategory, AdminExhibition, ContentSaveRequest } from '@/types/admin'
import { createContentForm, validateContentForm } from '@/utils/cms'
import { loadExhibitionOptions } from '@/utils/admin-options'

const route = useRoute()
const router = useRouter()
const editingId = computed(() => route.params.id ? Number(route.params.id) : null)
const form = reactive(createContentForm())
const formRef = ref<FormInstance>()
const categories = ref<AdminCategory[]>([])
const exhibitions = ref<AdminExhibition[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref(false)
const coverUploading = ref(false)
const bodyUploading = ref(false)
const rules: FormRules<ContentSaveRequest> = {
  categoryId: [{ required: true, type: 'number', min: 1, message: '请选择所属栏目', trigger: 'change' }],
  title: [{ required: true, whitespace: true, message: '请输入内容标题', trigger: 'blur' }],
  sortOrder: [{ required: true, type: 'integer', min: 0, message: '排序值必须为非负整数', trigger: 'change' }],
  extraData: [{ validator: (_rule, value: string | null, callback) => {
    try { if (value?.trim()) JSON.parse(value); callback() } catch { callback(new Error('请输入有效的 JSON')) }
  }, trigger: 'blur' }],
}
async function loadForm(): Promise<void> {
  loading.value = true
  error.value = false
  Object.assign(form, createContentForm())
  try {
    const results = await Promise.allSettled([cmsApi.categoryTree(), loadExhibitionOptions(), editingId.value ? cmsApi.content(editingId.value) : Promise.resolve(null)])
    if (results[0].status === 'fulfilled') categories.value = results[0].value
    else throw results[0].reason
    if (results[1].status === 'fulfilled') exhibitions.value = results[1].value
    else throw results[1].reason
    if (results[2].status === 'rejected') throw results[2].reason
    const detail = results[2].value
    if (detail) {
      for (const key of Object.keys(createContentForm()) as Array<keyof ContentSaveRequest>) Object.assign(form, { [key]: detail[key] })
    }
  } catch { error.value = true } finally { loading.value = false }
}
async function save(): Promise<void> {
  if (saving.value || coverUploading.value || bodyUploading.value) return
  if (!await formRef.value?.validate().catch(() => false)) return
  const valid = validateContentForm(form)
  if (valid !== true) { ElMessage.warning(valid); return }
  saving.value = true
  try {
    const payload = { ...form, title: form.title.trim(), exhibitionId: form.exhibitionId || null }
    if (editingId.value) await cmsApi.updateContent(editingId.value, payload)
    else await cmsApi.createContent(payload)
    ElMessage.success('内容已保存')
    await router.push({ name: 'admin-contents' })
  } catch { /* Retain form and let interceptor display the backend error. */ } finally { saving.value = false }
}
function goBack(): void { void router.push({ name: 'admin-contents' }) }
watch(() => route.params.id, () => { void loadForm() }, { immediate: true })
</script>

<style scoped>
@import '@/styles/admin-cms.css';
</style>
