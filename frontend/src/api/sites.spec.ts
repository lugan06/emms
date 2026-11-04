import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createManagedSiteForm } from '@/utils/sites'
const { get, post, put } = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn(), put: vi.fn() }))
vi.mock('@/utils/http', () => ({ http: { get, post, put } }))
import { sitesApi } from './sites'

beforeEach(() => vi.clearAllMocks())
describe('site management API', () => {
  it('fetches all grant options from the dedicated endpoint', async () => {
    get.mockResolvedValueOnce([{ id: 2, status: 'DISABLED' }])
    await expect(sitesApi.options()).resolves.toEqual([{ id: 2, status: 'DISABLED' }])
    expect(get).toHaveBeenCalledWith('/api/admin/sites/options')
  })
  it('passes pagination and filters and returns the page', async () => {
    const query = { keyword: 'expo', status: 'DISABLED' as const, page: 2, pageSize: 10 }
    const page = { records: [], total: 0, page: 2, pageSize: 10 }
    get.mockResolvedValueOnce(page)
    await expect(sitesApi.page(query)).resolves.toEqual(page)
    expect(get).toHaveBeenCalledWith('/api/admin/sites', { params: query })
    await sitesApi.page()
    expect(get).toHaveBeenLastCalledWith('/api/admin/sites', { params: {} })
  })
  it('creates, replaces and changes status using documented paths', async () => {
    const payload = createManagedSiteForm()
    await sitesApi.create(payload)
    await sitesApi.update(7, payload)
    await sitesApi.status(7, 'ENABLED')
    expect(post).toHaveBeenCalledWith('/api/admin/sites', payload)
    expect(put).toHaveBeenCalledWith('/api/admin/sites/7', payload)
    expect(put).toHaveBeenCalledWith('/api/admin/sites/7/status', { status: 'ENABLED' })
  })
  it('propagates backend errors to retain the page draft', async () => {
    post.mockRejectedValueOnce(new Error('域名已绑定其他站点'))
    await expect(sitesApi.create(createManagedSiteForm())).rejects.toThrow('域名已绑定其他站点')
    put.mockRejectedValueOnce(new Error('没有权限'))
    await expect(sitesApi.status(7, 'DISABLED')).rejects.toThrow('没有权限')
  })
})

