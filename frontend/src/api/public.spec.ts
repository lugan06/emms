import { describe, expect, it, vi } from 'vitest'

const { get } = vi.hoisted(() => ({ get: vi.fn() }))
vi.mock('@/utils/http', () => ({ http: { get } }))

describe('publicApi', () => {
  it('calls the public home endpoint', async () => {
    const { publicApi } = await import('./public')
    get.mockResolvedValueOnce({ site: null })

    await publicApi.home()

    expect(get).toHaveBeenCalledWith('/api/public/home')
  })

  it('serializes exhibition filters and pagination', async () => {
    const { publicApi } = await import('./public')
    get.mockResolvedValueOnce({ records: [], total: 0, page: 2, pageSize: 12 })

    await publicApi.exhibitions({ keyword: '涂料', year: 2026, page: 2, pageSize: 12 })

    expect(get).toHaveBeenCalledWith('/api/public/exhibitions', {
      params: { keyword: '涂料', year: 2026, page: 2, pageSize: 12 },
    })
  })

  it('loads category content with pagination', async () => {
    const { publicApi } = await import('./public')
    get.mockResolvedValueOnce({ records: [], total: 0, page: 1, pageSize: 10 })

    await publicApi.categoryContents('exhibition-news', { page: 1, pageSize: 10 })

    expect(get).toHaveBeenCalledWith('/api/public/categories/exhibition-news/contents', {
      params: { page: 1, pageSize: 10 },
    })
  })

  it('loads public content detail', async () => {
    const { publicApi } = await import('./public')
    get.mockResolvedValueOnce({ id: 10, title: '展会动态' })

    await publicApi.contentDetail(10)

    expect(get).toHaveBeenCalledWith('/api/public/contents/10')
  })
})
