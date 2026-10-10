<template>
  <main class="login-page">
    <el-card class="login-card" shadow="never">
      <div class="login-heading">
        <p class="eyebrow">EEMS ADMIN</p>
        <h1>Sign in</h1>
        <p>Access the administration workspace.</p>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="Username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="Password">
          <el-input v-model="form.password" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-button :loading="loading" native-type="submit" type="primary" class="submit-button">
          Sign in
        </el-button>
      </el-form>
    </el-card>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '@/stores/user'
import { isLoginFormValid } from '@/utils/validation'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function submit(): Promise<void> {
  if (!isLoginFormValid(form)) {
    ElMessage.warning('Please enter your username and password')
    return
  }

  loading.value = true
  try {
    await userStore.login(form)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin'
    await router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  padding: 24px;
  place-items: center;
  background: #f3f4f6;
}

.login-card {
  width: min(100%, 420px);
  border-radius: 8px;
}

.login-heading {
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
}

.login-heading p:last-child {
  margin: 8px 0 0;
  color: #6b7280;
}

.submit-button {
  width: 100%;
}
</style>
