import { describe, expect, it, vi } from 'vitest'

const { get, post, put, del } = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn(),
}))
vi.mock('@/utils/http', () => ({ http: { get, post, put, delete: del } }))

describe('adminApi', () => {
  it('loads exhibitions with admin filters', async () => {
    const { adminApi } = await import('./admin')
    get.mockResolvedValueOnce({ records: [], total: 0, page: 1, pageSize: 10 })

    await adminApi.exhibitions({ keyword: '绿色', status: 'DRAFT', year: 2027, page: 1, pageSize: 10 })

    expect(get).toHaveBeenCalledWith('/api/admin/exhibitions', {
      params: { keyword: '绿色', status: 'DRAFT', year: 2027, page: 1, pageSize: 10 },
    })
  })

  it('calls exhibition detail and status actions', async () => {
    const { adminApi } = await import('./admin')
    get.mockResolvedValueOnce({ id: 3 })
    post.mockResolvedValue({ id: 3 })
    del.mockResolvedValueOnce(undefined)

    await adminApi.exhibition(3)
    await adminApi.publishExhibition(3)
    await adminApi.offlineExhibition(3)
    await adminApi.deleteExhibition(3)

    expect(get).toHaveBeenCalledWith('/api/admin/exhibitions/3')
    expect(post).toHaveBeenNthCalledWith(1, '/api/admin/exhibitions/3/publish')
    expect(post).toHaveBeenNthCalledWith(2, '/api/admin/exhibitions/3/offline')
    expect(del).toHaveBeenCalledWith('/api/admin/exhibitions/3')
  })

  it('creates and updates exhibitions', async () => {
    const { adminApi } = await import('./admin')
    const payload = { exhibitionCode: 'EEMS-2027', title: '展会', year: 2027 }
    post.mockResolvedValueOnce({ id: 4 })
    put.mockResolvedValueOnce({ id: 4 })

    await adminApi.createExhibition(payload as never)
    await adminApi.updateExhibition(4, payload as never)

    expect(post).toHaveBeenCalledWith('/api/admin/exhibitions', payload)
    expect(put).toHaveBeenCalledWith('/api/admin/exhibitions/4', payload)
  })

  it('uploads a file as multipart form data', async () => {
    const { adminApi } = await import('./admin')
    post.mockResolvedValueOnce({ id: 8, fileUrl: '/uploads/cover.png' })
    const file = new File(['cover'], 'cover.png', { type: 'image/png' })

    await adminApi.uploadFile(file)

    const [path, body, config] = post.mock.calls.at(-1) as [string, FormData, { headers: Record<string, string> }]
    expect(path).toBe('/api/admin/files')
    expect(body.get('file')).toBe(file)
    expect(config.headers['Content-Type']).toBe('multipart/form-data')
  })
})
