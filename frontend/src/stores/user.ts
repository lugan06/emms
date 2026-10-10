import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { authApi } from '@/api/auth'
import type { LoginRequest } from '@/types/auth'
import { getToken, removeToken, setToken } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const isLoggedIn = computed(() => Boolean(token.value))

  async function login(payload: LoginRequest): Promise<void> {
    const result = await authApi.login(payload)
    setToken(result.token)
    token.value = result.token
  }

  function clearAuth(): void {
    removeToken()
    token.value = null
  }

  return {
    token,
    isLoggedIn,
    login,
    clearAuth,
  }
})
