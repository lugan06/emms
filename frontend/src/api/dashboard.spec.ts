import { beforeEach, describe, expect, it, vi } from 'vitest'
const { exhibitions, contents, guestbooks } = vi.hoisted(() => ({ exhibitions: vi.fn(), contents: vi.fn(), guestbooks: vi.fn() }))
vi.mock('@/api/admin', () => ({ adminApi: { exhibitions } }))
vi.mock('@/api/cms', () => ({ cmsApi: { contents } }))
vi.mock('@/api/system', () => ({ systemApi: { guestbooks } }))
import { loadDashboardStats } from './dashboard'
beforeEach(() => vi.clearAllMocks())
describe('dashboard statistics', () => {
  it('uses pagination totals and explicit status filters, including zero', async () => {
    exhibitions.mockResolvedValue({ total: 200, records: [{ id: 1 }] })
    contents.mockResolvedValue({ total: 60 }); guestbooks.mockResolvedValue({ total: 0 })
    expect(await loadDashboardStats()).toEqual([200, 60, 0])
    expect(exhibitions).toHaveBeenCalledWith({ page: 1, pageSize: 1 })
    expect(contents).toHaveBeenCalledWith({ status: 'PUBLISHED', page: 1, pageSize: 1 })
    expect(guestbooks).toHaveBeenCalledWith({ status: 'UNREAD', page: 1, pageSize: 1 })
  })
  it('retains successful counts and does not display failed counts as zero', async () => {
    exhibitions.mockResolvedValue({ total: 4 }); contents.mockRejectedValue(new Error('网络错误')); guestbooks.mockResolvedValue({ total: 3 })
    expect(await loadDashboardStats()).toEqual([4, null, 3])
  })
})
