import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { authApi } from '@/api/auth'
import type { AdminUser, LoginRequest } from '@/types/auth'
import { getAdminUser, getToken, removeAdminUser, removeToken, setAdminUser, setToken } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<AdminUser | null>(getAdminUser())
  const isLoggedIn = computed(() => Boolean(token.value))

  async function login(payload: LoginRequest): Promise<void> {
    const result = await authApi.login(payload)
    setToken(result.token)
    setAdminUser(result.user)
    token.value = result.token
    user.value = result.user
  }

  function clearAuth(): void {
    removeToken()
    removeAdminUser()
    token.value = null
    user.value = null
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    clearAuth,
  }
})
