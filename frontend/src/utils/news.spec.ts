import { beforeEach, describe, expect, it, vi } from 'vitest'
import { loadAllNews } from './news'
import type { PublicContentList } from '@/types/public'

const { categoryContents } = vi.hoisted(() => ({ categoryContents: vi.fn() }))
vi.mock('@/api/public', () => ({ publicApi: { categoryContents } }))
function item(id: number, publishedAt: string | null): PublicContentList {
  return { id, publishedAt, categoryId: 2, categoryCode: 'child-news', categoryName: '子栏目', exhibitionId: null,
    title: `新闻${id}`, slug: null, coverUrl: null, summary: null, author: null, source: null, isTop: 0, isRecommend: 0 }
}
beforeEach(() => { vi.resetAllMocks() })
describe('complete news feed', () => {
  it('loads every page without a fixed section limit and orders dates newest first', async () => {
    categoryContents.mockResolvedValueOnce({ records: Array.from({ length: 100 }, (_, i) => item(i + 1, '2026-01-01T10:00:00')), total: 102 })
      .mockResolvedValueOnce({ records: [item(101, '2026-02-01T10:00:00'), item(102, null)], total: 102 })
    const result = await loadAllNews()
    expect(categoryContents).toHaveBeenNthCalledWith(1, 'news', { page: 1, pageSize: 100, includeChildren: true, sort: 'LATEST' })
    expect(categoryContents).toHaveBeenNthCalledWith(2, 'news', { page: 2, pageSize: 100, includeChildren: true, sort: 'LATEST' })
    expect(result.total).toBe(102)
    expect(result.items).toHaveLength(102)
    expect(result.items.slice(0, 3).map(item => item.id)).toEqual([101, 100, 99])
    expect(result.items[101].id).toBe(102)
  })
  it('counts actual news and deduplicates page overlaps', async () => {
    categoryContents.mockResolvedValueOnce({ records: [item(1, null)], total: 1 })
    expect(await loadAllNews()).toEqual({ items: [item(1, null)], total: 1 })
    expect(categoryContents).toHaveBeenCalledOnce()
    categoryContents.mockResolvedValueOnce({ records: [item(2, null)], total: 101 })
      .mockResolvedValueOnce({ records: [item(2, null), item(3, null)], total: 101 })
    expect((await loadAllNews()).items.map(item => item.id)).toEqual([3, 2])
  })
  it('rejects a partially loaded feed when a later page fails', async () => {
    categoryContents.mockResolvedValueOnce({ records: [item(1, null)], total: 101 })
      .mockRejectedValueOnce(new Error('page failed'))
    await expect(loadAllNews()).rejects.toThrow('page failed')
  })
})
