import { describe, expect, it, vi } from 'vitest'

import type { LoginRequest } from '@/types/auth'

const { post } = vi.hoisted(() => ({ post: vi.fn() }))
vi.mock('@/utils/http', () => ({ http: { post } }))

describe('authApi', () => {
  it('sends login credentials to the auth endpoint', async () => {
    const { authApi } = await import('./auth')
    const payload: LoginRequest = { username: 'admin', password: 'secret' }
    post.mockResolvedValueOnce({ token: 'token' })

    await authApi.login(payload)

    expect(post).toHaveBeenCalledWith('/api/admin/login', payload)
  })
})
