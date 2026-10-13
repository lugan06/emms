<template>
  <div class="cms-page">
    <header class="cms-heading"><div><h1>栏目管理</h1><p>维护栏目层级、导航显示和内容模型。</p></div><el-button type="primary" @click="openCreate()">新增顶级栏目</el-button></header>
    <el-result v-if="error" icon="error" title="栏目加载失败"><template #extra><el-button @click="loadTree">重新加载</el-button></template></el-result>
    <section v-else class="cms-panel">
      <el-table v-loading="loading" :data="tree" row-key="id" default-expand-all :tree-props="{ children: 'children' }" empty-text="暂无栏目">
        <el-table-column prop="name" label="栏目名称" min-width="210" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="code" label="编码" min-width="160" show-overflow-tooltip />
        <el-table-column prop="modelCode" label="模型" width="115" />
        <el-table-column label="排序" width="195"><template #default="{ row }">
          <el-input-number v-model="sortDraft[row.id]" :min="0" :precision="0" controls-position="right" class="sort-input" :disabled="busy" />
          <el-button link type="primary" :disabled="busy || sortDraft[row.id] === row.sortOrder" @click="saveSort(row)">保存</el-button>
        </template></el-table-column>
        <el-table-column label="启用" width="75"><template #default="{ row }"><el-tag :type="row.isEnabled ? 'success' : 'info'">{{ row.isEnabled ? '是' : '否' }}</el-tag></template></el-table-column>
        <el-table-column label="导航" width="75"><template #default="{ row }"><el-tag :type="row.showInNav ? 'success' : 'info'">{{ row.showInNav ? '是' : '否' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" fixed="right" width="205"><template #default="{ row }">
          <el-button link type="primary" :disabled="busy" @click="openCreate(row.id)">新增子栏目</el-button>
          <el-button link type="primary" :disabled="busy" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" :disabled="busy" @click="remove(row)">删除</el-button>
        </template></el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogOpen" :title="editingId ? '编辑栏目' : '新增栏目'" width="min(760px, 94vw)" :close-on-click-modal="false" :close-on-press-escape="!saving" :show-close="!saving" @closed="formRef?.clearValidate()">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="saving" @submit.prevent="save">
        <div class="cms-form-grid">
          <el-form-item label="父栏目" prop="parentId">
            <el-tree-select v-model="form.parentId" :data="parentOptions" :props="{ label: 'name', value: 'id', children: 'children', disabled: 'disabled' }" node-key="id" check-strictly filterable default-expand-all class="full-width" />
          </el-form-item>
          <el-form-item label="栏目名称" prop="name"><el-input v-model="form.name" maxlength="100" /></el-form-item>
          <el-form-item label="编码 code" prop="code"><el-input v-model="form.code" maxlength="100" placeholder="全局唯一" /></el-form-item>
          <el-form-item label="URL 名称" prop="urlName"><el-input v-model="form.urlName" maxlength="150" /></el-form-item>
          <el-form-item label="模型" prop="modelCode"><el-select v-model="form.modelCode" class="full-width"><el-option v-for="option in categoryModelOptions" :key="option.value" :value="option.value" :label="`${option.label} (${option.value})`" /></el-select></el-form-item>
          <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="form.sortOrder" :min="0" :precision="0" class="full-width" /></el-form-item>
          <el-form-item label="列表模板"><el-input v-model="form.listTemplate" maxlength="100" /></el-form-item>
          <el-form-item label="详情模板"><el-input v-model="form.detailTemplate" maxlength="100" /></el-form-item>
          <el-form-item label="是否启用"><el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-form-item label="显示在导航"><el-switch v-model="form.showInNav" :active-value="1" :inactive-value="0" /></el-form-item>
        </div>
        <el-form-item label="跳转地址 linkUrl"><el-input v-model="form.linkUrl" maxlength="500" placeholder="LINK 模型的跳转地址" /></el-form-item>
      </el-form>
      <template #footer><el-button :disabled="saving" @click="dialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存栏目</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { cmsApi } from '@/api/cms'
import type { AdminCategory, CategorySaveRequest } from '@/types/admin'
import { categoryModelOptions, createCategoryForm, flattenCategories, forbiddenParentIds, validateCategoryParent } from '@/utils/cms'

const tree = ref<AdminCategory[]>([])
const loading = ref(false)
const error = ref(false)
const busy = ref(false)
const saving = ref(false)
const dialogOpen = ref(false)
const editingId = ref<number>()
const formRef = ref<FormInstance>()
const form = reactive(createCategoryForm())
const sortDraft = reactive<Record<number, number>>({})
interface ParentOption { id: number; name: string; disabled: boolean; children: ParentOption[] }
const parentOptions = computed<ParentOption[]>(() => {
  const forbidden = forbiddenParentIds(tree.value, editingId.value)
  function map(nodes: AdminCategory[]): ParentOption[] {
    return nodes.map(node => ({ id: node.id, name: node.name, disabled: forbidden.has(node.id), children: map(node.children) }))
  }
  return [{ id: 0, name: '顶级栏目', disabled: false, children: map(tree.value) }]
})
const requiredText = (message: string) => [{ required: true, whitespace: true, message, trigger: 'blur' }]
const rules: FormRules<CategorySaveRequest> = {
  name: requiredText('请输入栏目名称'), code: requiredText('请输入唯一编码'), modelCode: [{ required: true, message: '请选择模型', trigger: 'change' }],
  sortOrder: [{ type: 'integer', required: true, min: 0, message: '排序值必须为非负整数', trigger: 'change' }],
  parentId: [{ validator: (_rule, value: number, callback) => {
    const valid = validateCategoryParent(tree.value, value, editingId.value)
    callback(valid === true ? undefined : new Error(valid))
  }, trigger: 'change' }],
}
async function loadTree(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    tree.value = await cmsApi.categoryTree()
    for (const node of flattenCategories(tree.value)) sortDraft[node.id] = node.sortOrder
  } catch { error.value = true } finally { loading.value = false }
}
function openCreate(parentId = 0): void {
  editingId.value = undefined
  Object.assign(form, createCategoryForm(parentId))
  dialogOpen.value = true
}
function openEdit(row: AdminCategory): void {
  editingId.value = row.id
  for (const key of Object.keys(createCategoryForm()) as Array<keyof CategorySaveRequest>) {
    Object.assign(form, { [key]: row[key] })
  }
  dialogOpen.value = true
}
async function save(): Promise<void> {
  if (saving.value || !await formRef.value?.validate().catch(() => false)) return
  saving.value = true
  try {
    const payload = { ...form, name: form.name.trim(), code: form.code.trim() }
    if (editingId.value) await cmsApi.updateCategory(editingId.value, payload)
    else await cmsApi.createCategory(payload)
    ElMessage.success('栏目已保存')
    dialogOpen.value = false
    await loadTree()
  } catch { /* HTTP interceptor displays backend business errors. */ } finally { saving.value = false }
}
async function saveSort(row: AdminCategory): Promise<void> {
  const sortOrder = sortDraft[row.id]
  if (!Number.isInteger(sortOrder) || sortOrder < 0) { ElMessage.warning('排序值必须为非负整数'); return }
  busy.value = true
  try {
    await cmsApi.reorderCategories({ items: [{ id: row.id, parentId: row.parentId, sortOrder }] })
    ElMessage.success('排序已更新')
    await loadTree()
  } catch { /* Keep entered sort value on failure. */ } finally { busy.value = false }
}
async function remove(row: AdminCategory): Promise<void> {
  try { await ElMessageBox.confirm(`确定删除“${row.name}”吗？有子栏目或内容时不能删除。`, '删除栏目', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }) } catch { return }
  busy.value = true
  try { await cmsApi.deleteCategory(row.id); ElMessage.success('栏目已删除'); await loadTree() }
  catch { /* Preserve tree; interceptor shows the reason deletion was refused. */ } finally { busy.value = false }
}
onMounted(loadTree)
</script>

<style scoped>
@import '@/styles/admin-cms.css';
.sort-input { width: 115px; margin-right: 8px; }
</style>
