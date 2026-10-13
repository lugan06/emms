<template>
  <el-dialog v-model="visible" title="修改密码" width="min(480px, calc(100% - 32px))" :close-on-click-modal="false" :close-on-press-escape="!saving" :show-close="!saving" @closed="resetForm">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="saving" @submit.prevent="submit">
      <el-form-item label="原密码" prop="oldPassword" required>
        <el-input v-model="form.oldPassword" name="oldPassword" type="password" show-password autocomplete="current-password" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword" required>
        <el-input v-model="form.newPassword" name="newPassword" type="password" show-password autocomplete="new-password" placeholder="8–100 位" @input="revalidateConfirmation" />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword" required>
        <el-input v-model="form.confirmPassword" name="confirmPassword" type="password" show-password autocomplete="new-password" />
      </el-form-item>
      <div class="dialog-actions">
        <el-button :disabled="saving" @click="visible = false">取消</el-button>
        <el-button type="primary" native-type="submit" :loading="saving">确认修改</el-button>
      </div>
    </el-form>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { createPasswordForm, passwordErrors, type PasswordChangeForm } from '@/utils/account-forms'

const visible = defineModel<boolean>({ required: true })
const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const form = reactive(createPasswordForm())
const saving = ref(false)
const rules: FormRules<PasswordChangeForm> = Object.fromEntries(
  (Object.keys(form) as (keyof PasswordChangeForm)[]).map(field => [field, [{
    trigger: 'blur',
    validator: (_rule: unknown, _value: unknown, callback: (error?: Error) => void) => {
      const message = passwordErrors(form)[field]
      callback(message ? new Error(message) : undefined)
    },
  }]]),
)

function resetForm(): void {
  Object.assign(form, createPasswordForm())
  formRef.value?.clearValidate()
}

function revalidateConfirmation(): void {
  if (form.confirmPassword) void formRef.value?.validateField('confirmPassword').catch(() => {})
}

async function submit(): Promise<void> {
  if (saving.value || !formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || saving.value) return
  saving.value = true
  try {
    await authApi.changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    resetForm()
    visible.value = false
    userStore.clearAuth()
    ElMessage.success('密码修改成功，请重新登录')
    await router.replace({ name: 'admin-login' })
  } catch {
    // 请求封装统一显示错误，失败时不退出当前登录。
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.dialog-actions { display: flex; justify-content: flex-end; gap: 12px; padding-top: 8px; }
</style>
