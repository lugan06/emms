import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createCompanyForm, createSiteForm } from '@/utils/system'
const { get, put, post } = vi.hoisted(() => ({ get: vi.fn(), put: vi.fn(), post: vi.fn() }))
vi.mock('@/utils/http', () => ({ http: { get, put, post } }))
import { systemApi } from './system'
beforeEach(() => vi.clearAllMocks())
describe('system API contracts', () => {
  it('gets and saves only documented configuration fields', async () => {
    const site = { ...createSiteForm(), siteTitle: '站点', currentExhibitionId: 3 }
    const company = { ...createCompanyForm(), companyName: '公司' }
    await systemApi.site(); await systemApi.updateSite(site)
    await systemApi.company(); await systemApi.updateCompany(company)
    expect(get).toHaveBeenCalledWith('/api/admin/site')
    expect(put).toHaveBeenCalledWith('/api/admin/site', site)
    expect(get).toHaveBeenCalledWith('/api/admin/company')
    expect(put).toHaveBeenCalledWith('/api/admin/company', company)
  })
  it('passes guestbook pagination and uses replyContent in replies', async () => {
    const query = { status: 'UNREAD' as const, page: 2, pageSize: 10 }
    await systemApi.guestbooks(query); await systemApi.readGuestbook(7)
    await systemApi.replyGuestbook(7, { replyContent: '回复' }); await systemApi.closeGuestbook(7)
    expect(get).toHaveBeenCalledWith('/api/admin/guestbooks', { params: query })
    expect(post).toHaveBeenCalledWith('/api/admin/guestbooks/7/read')
    expect(post).toHaveBeenCalledWith('/api/admin/guestbooks/7/reply', { replyContent: '回复' })
    expect(post).toHaveBeenCalledWith('/api/admin/guestbooks/7/close')
  })
  it('passes the entire log query using backend date and result fields', async () => {
    const query = { username: 'admin', module: 'GUESTBOOK', operation: '回复', result: 'SUCCESS' as const,
      startAt: '2026-10-01 00:00:00', endAt: '2026-10-31 23:59:59', page: 2, pageSize: 20 }
    const logs = { records: [{ requestParams: '{"message":"***"}' }], total: 1 }
    get.mockResolvedValueOnce(logs)
    await expect(systemApi.logs(query)).resolves.toBe(logs)
    expect(get).toHaveBeenCalledWith('/api/admin/logs', { params: query })
  })
  it('propagates a permission or business failure', async () => {
    put.mockRejectedValueOnce(new Error('无权限'))
    await expect(systemApi.updateSite(createSiteForm())).rejects.toThrow('无权限')
    post.mockRejectedValueOnce(new Error('已关闭留言不能回复'))
    await expect(systemApi.replyGuestbook(7, { replyContent: '回复' })).rejects.toThrow('已关闭')
  })
})
