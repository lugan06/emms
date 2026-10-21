<template>
  <div class="page-view">
    <header class="page-heading">
      <div>
        <p class="eyebrow">EXHIBITION MANAGEMENT</p>
        <h1>{{ isEditing ? '编辑展会' : '新增展会' }}</h1>
        <p>按接口字段维护展会信息，保存后可在列表中发布或下架。</p>
      </div>
      <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
    </header>

    <el-skeleton v-if="loading" :rows="16" animated class="form-skeleton" />
    <el-form
      v-else
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="exhibition-form"
      @submit.prevent="submit"
    >
      <section class="form-section">
        <div class="section-heading">
          <h2>基础信息</h2>
          <span>必填字段标有 *</span>
        </div>
        <div class="form-grid two-columns">
          <el-form-item label="业务编码" prop="exhibitionCode" required>
            <el-input v-model="form.exhibitionCode" maxlength="100" show-word-limit placeholder="例如 EEMS-2027" />
          </el-form-item>
          <el-form-item label="展会标题" prop="title" required>
            <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="请输入展会标题" />
          </el-form-item>
          <el-form-item label="副标题" prop="subtitle">
            <el-input v-model="form.subtitle" maxlength="200" placeholder="可选" />
          </el-form-item>
          <el-form-item label="举办年份" prop="year" required>
            <el-input-number v-model="form.year" :min="2000" :max="2200" controls-position="right" class="full-width" />
          </el-form-item>
          <el-form-item label="届次" prop="edition">
            <el-input v-model="form.edition" maxlength="50" placeholder="例如 第十九届" />
          </el-form-item>
          <el-form-item label="封面图" prop="coverUrl">
            <CoverImageUpload v-model="form.coverUrl" :disabled="saving" @uploading-change="uploading = $event" />
          </el-form-item>
        </div>
      </section>

      <section class="form-section">
        <div class="section-heading"><h2>时间与地点</h2></div>
        <div class="form-grid two-columns">
          <el-form-item label="开始时间" prop="startAt" required>
            <el-date-picker
              v-model="form.startAt"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择开始时间"
              class="full-width"
            />
          </el-form-item>
          <el-form-item label="结束时间" prop="endAt" required>
            <el-date-picker
              v-model="form.endAt"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择结束时间"
              class="full-width"
            />
          </el-form-item>
          <el-form-item label="展馆或举办地点" prop="venue">
            <el-input v-model="form.venue" maxlength="200" placeholder="例如 国家会展中心（上海）" />
          </el-form-item>
          <el-form-item label="详细地址" prop="address">
            <el-input v-model="form.address" maxlength="500" placeholder="请输入详细地址" />
          </el-form-item>
        </div>
      </section>

      <section class="form-section">
        <div class="section-heading"><h2>展会介绍</h2></div>
        <el-form-item label="展会简介" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="用于列表和首页摘要" />
        </el-form-item>
        <el-form-item label="展会详细描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="7" placeholder="请输入展会详细描述" />
        </el-form-item>
      </section>

      <section class="form-section">
        <div class="section-heading"><h2>联系与报名</h2></div>
        <div class="form-grid two-columns">
          <el-form-item label="报名地址" prop="registrationUrl">
            <el-input v-model="form.registrationUrl" maxlength="500" placeholder="可选" />
          </el-form-item>
          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="form.contactName" maxlength="100" placeholder="可选" />
          </el-form-item>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" maxlength="30" placeholder="可选" />
          </el-form-item>
        </div>
      </section>

      <section class="form-section">
        <div class="section-heading"><h2>首页与扩展配置</h2></div>
        <div class="form-grid two-columns">
          <el-form-item label="首页展示" prop="showOnHome">
            <el-switch v-model="form.showOnHome" :active-value="1" :inactive-value="0" active-text="展示" inactive-text="不展示" />
          </el-form-item>
          <el-form-item label="首页排序值" prop="homeSort">
            <el-input-number v-model="form.homeSort" :min="0" :step="1" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item label="扩展属性 JSON" prop="extraData">
          <el-input v-model="form.extraData" type="textarea" :rows="4" placeholder='例如 {"hall":"N1-N3"}' />
        </el-form-item>
      </section>

      <div class="form-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="uploading" native-type="submit">保存展会</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

import { adminApi } from '@/api/admin'
import type { AdminExhibitionSaveRequest } from '@/types/admin'
import CoverImageUpload from '@/components/admin/CoverImageUpload.vue'
import { createExhibitionForm, validateExhibitionTime } from '@/utils/exhibition'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const form = reactive<AdminExhibitionSaveRequest>(createExhibitionForm())
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const isEditing = computed(() => Boolean(route.params.id))

const rules: FormRules<AdminExhibitionSaveRequest> = {
  exhibitionCode: [{ required: true, message: '请输入业务编码', trigger: 'blur' }],
  title: [{ required: true, message: '请输入展会标题', trigger: 'blur' }],
  year: [{ required: true, message: '请选择举办年份', trigger: 'change' }],
  startAt: [
    { required: true, message: '请选择开始时间', trigger: 'change' },
    { validator: (_rule, _value, callback) => callback(validateExhibitionTime(form.startAt, form.endAt) === true ? undefined : new Error(validateExhibitionTime(form.startAt, form.endAt) as string)), trigger: 'change' },
  ],
  endAt: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    { validator: (_rule, _value, callback) => callback(validateExhibitionTime(form.startAt, form.endAt) === true ? undefined : new Error(validateExhibitionTime(form.startAt, form.endAt) as string)), trigger: 'change' },
  ],
}

async function loadExhibition(): Promise<void> {
  const id = Number(route.params.id)
  if (!isEditing.value || !Number.isInteger(id) || id <= 0) {
    return
  }

  loading.value = true
  try {
    const detail = await adminApi.exhibition(id)
    Object.assign(form, {
      exhibitionCode: detail.exhibitionCode,
      title: detail.title,
      subtitle: detail.subtitle,
      year: detail.year,
      edition: detail.edition,
      coverUrl: detail.coverUrl,
      summary: detail.summary,
      description: detail.description,
      venue: detail.venue,
      address: detail.address,
      startAt: detail.startAt,
      endAt: detail.endAt,
      registrationUrl: detail.registrationUrl,
      contactName: detail.contactName,
      contactPhone: detail.contactPhone,
      showOnHome: detail.showOnHome,
      homeSort: detail.homeSort,
      extraData: detail.extraData,
    })
  } finally {
    loading.value = false
  }
}

async function submit(): Promise<void> {
  if (uploading.value || saving.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const id = Number(route.params.id)
    if (isEditing.value) {
      await adminApi.updateExhibition(id, form)
      ElMessage.success('展会已更新')
    } else {
      await adminApi.createExhibition(form)
      ElMessage.success('展会已创建')
    }
    await router.replace({ name: 'admin-exhibitions' })
  } finally {
    saving.value = false
  }
}

function goBack(): void {
  void router.push({ name: 'admin-exhibitions' })
}

watch(() => route.params.id, () => {
  void loadExhibition()
}, { immediate: true })
</script>

<style scoped>
.page-view {
  display: grid;
  gap: 20px;
}

.page-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.page-heading h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
}

.page-heading p:last-child {
  margin: 8px 0 0;
  color: #6b7280;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.exhibition-form,
.form-skeleton {
  display: grid;
  gap: 20px;
}

.form-section {
  padding: 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.section-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.section-heading h2 {
  margin: 0;
  color: #1f2937;
  font-size: 18px;
}

.section-heading span {
  color: #9ca3af;
  font-size: 12px;
}

.form-grid {
  display: grid;
  gap: 4px 20px;
}

.two-columns {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.full-width {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 680px) {
  .page-heading {
    flex-direction: column;
  }

  .two-columns {
    grid-template-columns: 1fr;
  }

  .form-section {
    padding: 18px;
  }
}
</style>
