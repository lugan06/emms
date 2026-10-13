import { describe, expect, it, vi } from 'vitest'

import type { LoginRequest } from '@/types/auth'

const { post, get, put } = vi.hoisted(() => ({ post: vi.fn(), get: vi.fn(), put: vi.fn() }))
vi.mock('@/utils/http', () => ({ http: { post, get, put } }))

describe('authApi', () => {
  it('changes the password with PUT and preserves backend failures', async () => {
    const { authApi } = await import('./auth')
    const payload = { oldPassword: 'old-test-password', newPassword: 'new-test-password' }
    put.mockResolvedValueOnce(null)
    expect(await authApi.changePassword(payload)).toBeNull()
    expect(put).toHaveBeenCalledWith('/api/admin/password', payload)
    put.mockRejectedValueOnce(new Error('原密码错误'))
    await expect(authApi.changePassword(payload)).rejects.toThrow('原密码错误')
  })
  it('refreshes the current administrator through the documented me endpoint', async () => {
    const { authApi } = await import('./auth')
    await authApi.me()
    expect(get).toHaveBeenCalledWith('/api/admin/me')
  })
  it('sends login credentials to the auth endpoint', async () => {
    const { authApi } = await import('./auth')
    const payload: LoginRequest = { username: 'admin', password: 'secret' }
    post.mockResolvedValueOnce({ token: 'token' })

    await authApi.login(payload)

    expect(post).toHaveBeenCalledWith('/api/admin/login', payload)
  })
})
