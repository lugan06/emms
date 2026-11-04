import { beforeEach, describe, expect, it, vi } from 'vitest'
const { get, post, put } = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn(), put: vi.fn() }))
vi.mock('@/utils/http', () => ({ http: { get, post, put } }))
import { usersApi } from './users'
beforeEach(() => vi.clearAllMocks())

describe('administrator management API', () => {
  it('forwards all pagination and search filters', async () => {
    const params = { keyword: 'editor', status: 'ENABLED' as const, siteId: 8, page: 2, pageSize: 10 }
    const response = { records: [], total: 0, page: 2, pageSize: 10 }
    get.mockResolvedValueOnce(response)
    await expect(usersApi.page(params)).resolves.toBe(response)
    expect(get).toHaveBeenCalledWith('/api/admin/users', { params })
    await usersApi.page()
    expect(get).toHaveBeenLastCalledWith('/api/admin/users', { params: {} })
  })
  it('uses separate commands for each management operation and sends no role', async () => {
    const payload = { username: 'editor', password: 'initial-password', nickname: 'Editor', siteIds: [1, 2] }
    await usersApi.create(payload); await usersApi.update(7, 'New name')
    await usersApi.sites(7, [2]); await usersApi.status(7, 'DISABLED'); await usersApi.password(7, 'new-password')
    expect(post).toHaveBeenCalledWith('/api/admin/users', payload)
    expect(put).toHaveBeenCalledWith('/api/admin/users/7', { nickname: 'New name' })
    expect(put).toHaveBeenCalledWith('/api/admin/users/7/sites', { siteIds: [2] })
    expect(put).toHaveBeenCalledWith('/api/admin/users/7/status', { status: 'DISABLED' })
    expect(put).toHaveBeenCalledWith('/api/admin/users/7/password', { password: 'new-password' })
    await usersApi.sites(7, [])
    expect(put).toHaveBeenLastCalledWith('/api/admin/users/7/sites', { siteIds: [] })
  })
  it('propagates business and authorization failures', async () => {
    post.mockRejectedValueOnce(new Error('用户名已存在'))
    await expect(usersApi.create({ username: 'u', password: 'password', nickname: 'U', siteIds: [] })).rejects.toThrow('用户名已存在')
    put.mockRejectedValueOnce(new Error('普通管理员不存在'))
    await expect(usersApi.password(1, 'password')).rejects.toThrow('普通管理员不存在')
  })
})

