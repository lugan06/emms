<template>
  <section id="online-guestbook" class="guestbook-section" aria-labelledby="guestbook-heading">
    <el-card shadow="never">
      <template #header>
        <h2 id="guestbook-heading">在线留言</h2>
        <p>欢迎留下您的问题或建议，我们会尽快与您联系。</p>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="submitting" @submit.prevent="submit">
        <div class="contact-grid">
          <el-form-item label="姓名" prop="name" required>
            <el-input v-model="form.name" name="name" autocomplete="name" maxlength="100" placeholder="请输入您的姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" name="phone" autocomplete="tel" maxlength="30" placeholder="选填" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" name="email" autocomplete="email" maxlength="150" placeholder="选填" />
          </el-form-item>
          <el-form-item label="公司名称" prop="companyName">
            <el-input v-model="form.companyName" name="companyName" autocomplete="organization" maxlength="200" placeholder="选填" />
          </el-form-item>
        </div>
        <el-form-item label="留言内容" prop="message" required>
          <el-input v-model="form.message" name="message" type="textarea" :rows="4" maxlength="2000" show-word-limit placeholder="请输入您的问题或建议" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="submitting">提交留言</el-button>
      </el-form>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { publicApi } from '@/api/public'
import { createGuestbookForm, guestbookErrors, guestbookPayload, type GuestbookForm } from '@/utils/account-forms'

const form = reactive(createGuestbookForm())
const formRef = ref<FormInstance>()
const submitting = ref(false)
const rules: FormRules<GuestbookForm> = Object.fromEntries(
  (Object.keys(form) as (keyof GuestbookForm)[]).map(field => [field, [{
    trigger: 'blur',
    validator: (_rule: unknown, _value: unknown, callback: (error?: Error) => void) => {
      const message = guestbookErrors(form)[field]
      callback(message ? new Error(message) : undefined)
    },
  }]]),
)

async function submit(): Promise<void> {
  if (submitting.value || !formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || submitting.value) return
  submitting.value = true
  try {
    await publicApi.submitGuestbook(guestbookPayload(form))
    Object.assign(form, createGuestbookForm())
    formRef.value.clearValidate()
    ElMessage.success('留言提交成功')
  } catch {
    // 请求封装统一提示失败，保留用户已填写的留言，便于重试。
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.guestbook-section { width: min(1180px, calc(100% - 48px)); margin: 0 auto; padding-bottom: 48px; }
h2 { margin: 0; font-size: 22px; color: #111827; }
p { margin: 8px 0 0; color: #6b7280; font-size: 14px; }
.contact-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 24px; }
@media (max-width: 760px) {
  .guestbook-section { width: calc(100% - 32px); }
  .contact-grid { grid-template-columns: 1fr; }
}
</style>
