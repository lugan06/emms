import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

import type { ApiErrorPayload, ApiResponse } from '@/types/api'
import { pinia } from '@/stores'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const configuredBaseUrl = String(import.meta.env.VITE_API_BASE_URL || '')
  .replace(/\/+$/, '')
  .replace(/\/api$/i, '')

const http = axios.create({
  baseURL: configuredBaseUrl,
  timeout: Number(import.meta.env.VITE_API_TIMEOUT ?? 10000),
  headers: {
    'Content-Type': 'application/json',
  },
})

const siteErrorMessages: Record<string, string> = {
  SITE_ADMIN_FORBIDDEN: '当前域名无管理权限',
  SITE_DISABLED: '站点已停用',
  SITE_NOT_FOUND: '当前域名未绑定站点',
  DEFAULT_SITE_MISSING: '默认站点未配置',
}

function getErrorCode(payload: ApiErrorPayload | undefined): string | undefined {
  if (payload?.code === undefined || payload.code === null) return undefined
  return String(payload.code)
}

function getDisplayMessage(code: string | undefined, fallback: string): string {
  return (code && siteErrorMessages[code]) || fallback
}

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
      const message = getDisplayMessage(code, payload.message || 'Request failed')
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    // The interceptor unwraps the envelope at runtime; Axios types model the raw response here.
    return payload.data as never
  },
  async (error: AxiosError<ApiErrorPayload>) => {
    if (error.response?.status === 401) {
      await redirectToLogin()
    }

    const code = getErrorCode(error.response?.data)
    const message = getDisplayMessage(code, error.response?.data?.message ?? error.message ?? 'Network error')
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export { http }
