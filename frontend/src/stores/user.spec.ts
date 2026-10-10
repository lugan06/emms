import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import { useUserStore } from './user'

vi.mock('@/api/auth', () => ({
  authApi: {
    login: vi.fn().mockResolvedValue({ token: 'new-token' }),
  },
}))

describe('user store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('restores a token from storage', () => {
    localStorage.setItem('eems:access-token', 'stored-token')

    const store = useUserStore()

    expect(store.token).toBe('stored-token')
    expect(store.isLoggedIn).toBe(true)
  })

  it('stores the token after login and clears auth', async () => {
    const store = useUserStore()

    await store.login({ username: 'admin', password: 'secret' })
    expect(store.token).toBe('new-token')
    expect(localStorage.getItem('eems:access-token')).toBe('new-token')

    store.clearAuth()
    expect(store.token).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(localStorage.getItem('eems:access-token')).toBeNull()
  })
})
