import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { authApi } from '@/api/auth'
import type { AdminUser, LoginRequest } from '@/types/auth'
import { getAdminUser, getToken, removeAdminUser, removeToken, setAdminUser, setToken } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<AdminUser | null>(getAdminUser())
  const isLoggedIn = computed(() => Boolean(token.value))
  const userVerified = ref(false)
  let refreshPromise: Promise<void> | null = null

  async function refreshUser(): Promise<void> {
    if (!refreshPromise) {
      const activeToken = token.value
      refreshPromise = authApi.me().then(result => {
        if (token.value !== activeToken || !activeToken) return
        user.value = result
        setAdminUser(result)
        userVerified.value = true
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
  }

  function clearAuth(): void {
    removeToken()
    removeAdminUser()
    token.value = null
    user.value = null
    userVerified.value = false
  }

  return {
    token,
    user,
    isLoggedIn,
    userVerified,
    refreshUser,
    login,
    clearAuth,
  }
})
