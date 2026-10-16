<template>
  <main class="login-page">
    <div class="login-shell" aria-label="会展管理系统登录">
      <div class="login-mark" aria-hidden="true">
        <span class="mark-bar mark-bar-one"></span>
        <span class="mark-bar mark-bar-two"></span>
        <span class="mark-bar mark-bar-three"></span>
      </div>
      <el-card class="login-card" shadow="never">
      <div class="login-heading">
        <p class="eyebrow">管理员登录</p>
        <h1>会展管理系统</h1>
        <p>登录后台，管理展会与网站内容</p>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password autocomplete="current-password" placeholder="请输入密码" />
        </el-form-item>
        <el-button :loading="loading" native-type="submit" type="primary" class="submit-button">
          登录系统
        </el-button>
      </el-form>
      </el-card>
    </div>
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
    ElMessage.warning('请输入用户名和密码')
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
  position: relative;
  display: grid;
  overflow: hidden;
  min-height: 100vh;
  padding: 24px;
  place-items: center;
  background: #eef2f6;
}

.login-page::before,
.login-page::after {
  position: absolute;
  width: 160px;
  height: 160px;
  content: '';
  border: 1px solid rgb(37 99 235 / 14%);
  transform: rotate(45deg);
}

.login-page::before {
  top: -92px;
  left: -70px;
}

.login-page::after {
  right: -70px;
  bottom: -92px;
  border-color: rgb(15 23 42 / 10%);
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(100%, 430px);
}

.login-card {
  overflow: visible;
  border: 1px solid #dbe3ed;
  border-radius: 18px;
  box-shadow: 0 22px 50px rgb(15 23 42 / 12%);
}

.login-card :deep(.el-card__body) {
  padding: 38px 40px 40px;
}

.login-mark {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  width: 66px;
  height: 66px;
  margin: 0 auto -18px;
  padding: 15px;
  position: relative;
  z-index: 2;
  background: #172033;
  border: 6px solid #eef2f6;
  border-radius: 20px;
  box-shadow: 0 10px 20px rgb(15 23 42 / 16%);
  transform: rotate(-6deg);
}

.mark-bar {
  display: block;
  width: 7px;
  margin: 0 2px;
  background: #60a5fa;
  border-radius: 4px 4px 2px 2px;
  transform: rotate(6deg);
}

.mark-bar-one { height: 18px; }
.mark-bar-two { height: 28px; background: #93c5fd; }
.mark-bar-three { height: 22px; }

.login-heading {
  text-align: center;
}

.eyebrow {
  margin: 0 0 10px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

h1 {
  margin: 0 0 10px;
  color: #111827;
  font-size: 30px;
  letter-spacing: 0;
}

.login-heading p:last-child {
  margin: 0 0 30px;
  color: #6b7280;
  font-size: 14px;
}

.login-card :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-card :deep(.el-form-item__label) {
  padding-bottom: 8px;
  color: #273449;
  font-size: 13px;
  font-weight: 650;
}

.login-card :deep(.el-input__wrapper) {
  min-height: 46px;
  padding: 1px 16px;
  background: #f8fafc;
  border: 1px solid #dbe3ed;
  border-radius: 12px;
  box-shadow: none;
  transition: border-color 160ms ease, box-shadow 160ms ease, background 160ms ease;
}

.login-card :deep(.el-input__wrapper:hover) {
  border-color: #93c5fd;
}

.login-card :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  border-color: #2563eb;
  box-shadow: 0 0 0 4px rgb(37 99 235 / 12%);
}

.login-card :deep(.el-input__inner) {
  color: #111827;
  font-size: 14px;
}

.login-card :deep(.el-input__inner::placeholder) {
  color: #9aa7b8;
}

.submit-button {
  width: 100%;
  height: 46px;
  margin-top: 6px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  background: #2563eb;
  border: 0;
  border-radius: 12px;
  box-shadow: 0 8px 16px rgb(37 99 235 / 22%);
  transition: background 160ms ease, box-shadow 160ms ease, transform 160ms ease;
}

.submit-button:hover {
  background: #1d4ed8;
  box-shadow: 0 10px 20px rgb(37 99 235 / 28%);
  transform: translateY(-1px);
}

.submit-button:active {
  box-shadow: 0 4px 10px rgb(37 99 235 / 18%);
  transform: translateY(0);
}

@media (max-width: 520px) {
  .login-page {
    padding: 20px;
  }

  .login-card :deep(.el-card__body) {
    padding: 34px 24px 28px;
  }

}
</style>
