import { describe, expect, it, vi } from 'vitest'

const { get, post } = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn() }))
vi.mock('@/utils/http', () => ({ http: { get, post } }))

describe('publicApi', () => {
  it('submits the documented guestbook fields and propagates business failures', async () => {
    const { publicApi } = await import('./public')
    const payload = { name: '访客', phone: null, email: null, companyName: null, message: '咨询展会信息' }
    post.mockResolvedValueOnce(null)
    expect(await publicApi.submitGuestbook(payload)).toBeNull()
    expect(post).toHaveBeenCalledWith('/api/public/guestbook', payload)
    post.mockRejectedValueOnce(new Error('请求过于频繁'))
    await expect(publicApi.submitGuestbook(payload)).rejects.toThrow('请求过于频繁')
  })
  it('calls the public home endpoint', async () => {
    const { publicApi } = await import('./public')
    const response = { exhibitionIntro: [{ id: 102, body: '<p>完整正文</p>' }], exhibitionScope: [{ id: 104, body: '<ul><li>参展范围</li></ul>' }] }
    get.mockResolvedValueOnce(response)

    expect(await publicApi.home()).toEqual(response)

    expect(get).toHaveBeenCalledWith('/api/public/home')
  })

  it('loads configured company contact information and propagates failures', async () => {
    const { publicApi } = await import('./public')
    const company = { companyName: '测试公司', telephone: '021-12345678' }
    get.mockResolvedValueOnce(company)
    expect(await publicApi.company()).toEqual(company)
    expect(get).toHaveBeenCalledWith('/api/public/company')
    get.mockRejectedValueOnce(new Error('加载失败'))
    await expect(publicApi.company()).rejects.toThrow('加载失败')
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
