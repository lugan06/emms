import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

import { useUserStore } from './user'
import { authApi } from '@/api/auth'
import { systemApi } from '@/api/system'

vi.mock('@/api/auth', () => ({
  authApi: {
    me: vi.fn(),
    login: vi.fn().mockResolvedValue({
      token: 'new-token',
      user: { id: 1, username: 'admin', nickname: '管理员', avatarUrl: null, roleCode: 'ADMIN' },
    }),
  },
}))
vi.mock('@/api/system', () => ({
  systemApi: { site: vi.fn() },
}))

describe('user store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.mocked(authApi.me).mockReset()
    vi.mocked(systemApi.site).mockReset()
    vi.mocked(systemApi.site).mockResolvedValue({ siteTitle: '默认站点' } as never)
  })

  it('restores a token from storage', () => {
    localStorage.setItem('eems:access-token', 'stored-token')

    const store = useUserStore()

    expect(store.token).toBe('stored-token')
    expect(store.isLoggedIn).toBe(true)
  })

  it('replaces the cached role with the role returned by the backend and persists it', async () => {
    localStorage.setItem('eems:access-token', 'stored-token')
    localStorage.setItem('eems:admin-user', JSON.stringify({ roleCode: 'SUPER_ADMIN' }))
    const store = useUserStore()
    vi.mocked(authApi.me).mockResolvedValue({ id: 2, username: 'editor', nickname: null, avatarUrl: null, roleCode: 'EDITOR' })
    expect(store.userVerified).toBe(false)
    await store.refreshUser()
    expect(store.user?.roleCode).toBe('EDITOR')
    expect(store.siteName).toBe('默认站点')
    expect(store.userVerified).toBe(true)
    expect(JSON.parse(localStorage.getItem('eems:admin-user')!).roleCode).toBe('EDITOR')
  })

  it('does not restore a user from a late response after logout', async () => {
    localStorage.setItem('eems:access-token', 'stored-token')
    const store = useUserStore()
    let resolve!: (user: Awaited<ReturnType<typeof authApi.me>>) => void
    vi.mocked(authApi.me).mockReturnValue(new Promise(done => { resolve = done }))
    const pending = store.refreshUser()
    store.clearAuth()
    resolve({ id: 1, username: 'admin', nickname: null, avatarUrl: null, roleCode: 'SUPER_ADMIN' })
    await pending
    expect(store.user).toBeNull(); expect(store.userVerified).toBe(false)
  })

  it('keeps the role unverified when the backend refresh fails', async () => {
    localStorage.setItem('eems:access-token', 'stored-token')
    const store = useUserStore()
    vi.mocked(authApi.me).mockRejectedValue(new Error('401'))
    await expect(store.refreshUser()).rejects.toThrow('401')
    expect(store.userVerified).toBe(false)
  })

  it('keeps the user verified when the site configuration refresh fails', async () => {
    localStorage.setItem('eems:access-token', 'stored-token')
    const store = useUserStore()
    vi.mocked(authApi.me).mockResolvedValue({ id: 2, username: 'editor', nickname: null, avatarUrl: null, roleCode: 'EDITOR' })
    vi.mocked(systemApi.site).mockRejectedValue(new Error('site unavailable'))

    await store.refreshUser()

    expect(store.userVerified).toBe(true)
    expect(store.siteName).toBeNull()
  })

  it('stores the token after login and clears auth', async () => {
    const store = useUserStore()

    await store.login({ username: 'admin', password: 'secret' })
    expect(store.token).toBe('new-token')
    expect(store.user?.username).toBe('admin')
    expect(store.siteName).toBe('默认站点')
    expect(localStorage.getItem('eems:access-token')).toBe('new-token')

    store.clearAuth()
    expect(store.token).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(store.user).toBeNull()
    expect(store.siteName).toBeNull()
    expect(localStorage.getItem('eems:access-token')).toBeNull()
  })
})
