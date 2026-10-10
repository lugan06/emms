import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

import type { ApiErrorPayload, ApiResponse } from '@/types/api'
import { pinia } from '@/stores'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: Number(import.meta.env.VITE_API_TIMEOUT ?? 10000),
  headers: {
    'Content-Type': 'application/json',
  },
})

async function redirectToLogin(): Promise<void> {
  useUserStore(pinia).clearAuth()
  if (router.currentRoute.value.path !== '/admin/login') {
    await router.replace({
      path: '/admin/login',
      query: { redirect: router.currentRoute.value.fullPath },
    })
  }
}

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useUserStore(pinia).token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResponse<unknown>
    const code = String(payload.code)
    if (code === '401' || code === 'UNAUTHORIZED') {
      void redirectToLogin()
      ElMessage.error(payload.message || 'Please sign in again')
      return Promise.reject(new Error(payload.message || 'Unauthorized'))
    }
    if (!['OK', '0', '200', 'SUCCESS'].includes(code)) {
      ElMessage.error(payload.message || 'Request failed')
      return Promise.reject(new Error(payload.message || 'Request failed'))
    }
    // The interceptor unwraps the envelope at runtime; Axios types model the raw response here.
    return payload.data as never
  },
  async (error: AxiosError<ApiErrorPayload>) => {
    if (error.response?.status === 401) {
      await redirectToLogin()
    }

    const message = error.response?.data?.message ?? error.message ?? 'Network error'
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export { http }
