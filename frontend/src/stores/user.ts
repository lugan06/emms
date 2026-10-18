import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { authApi } from '@/api/auth'
import type { AdminUser, LoginRequest } from '@/types/auth'
import { getAdminUser, getToken, removeAdminUser, removeToken, setAdminUser, setToken } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<AdminUser | null>(getAdminUser())
  const siteName = ref<string | null>(null)
  const isLoggedIn = computed(() => Boolean(token.value))
  const userVerified = ref(false)
  let refreshPromise: Promise<void> | null = null
  let siteRefreshPromise: Promise<void> | null = null

  async function refreshSite(): Promise<void> {
    const activeToken = token.value
    if (!activeToken) {
      siteName.value = null
      return
    }
    if (!siteRefreshPromise) {
      siteRefreshPromise = import('@/api/system').then(({ systemApi }) => systemApi.site()).then(result => {
        if (token.value !== activeToken) return
        siteName.value = result.siteTitle?.trim() || null
      }).finally(() => { siteRefreshPromise = null })
    }
    await siteRefreshPromise
  }

  async function refreshUser(): Promise<void> {
    if (!refreshPromise) {
      const activeToken = token.value
      refreshPromise = authApi.me().then(result => {
        if (token.value !== activeToken || !activeToken) return
        user.value = result
        setAdminUser(result)
        userVerified.value = true
        return refreshSite().catch(() => {
          if (token.value === activeToken) siteName.value = null
        })
      }).finally(() => { refreshPromise = null })
    }
    await refreshPromise
  }

  async function login(payload: LoginRequest): Promise<void> {
    const result = await authApi.login(payload)
    setToken(result.token)
    setAdminUser(result.user)
    token.value = result.token
    user.value = result.user
    userVerified.value = true
    await refreshSite().catch(() => { siteName.value = null })
  }

  function clearAuth(): void {
    removeToken()
    removeAdminUser()
    token.value = null
    user.value = null
    siteName.value = null
    userVerified.value = false
  }

  return {
    token,
    user,
    siteName,
    isLoggedIn,
    userVerified,
    refreshUser,
    refreshSite,
    login,
    clearAuth,
  }
})
