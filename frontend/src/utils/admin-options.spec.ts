import { beforeEach, describe, expect, it, vi } from 'vitest'
const { exhibitions } = vi.hoisted(() => ({ exhibitions: vi.fn() }))
vi.mock('@/api/admin', () => ({ adminApi: { exhibitions } }))
import { loadExhibitionOptions } from './admin-options'
beforeEach(() => vi.clearAllMocks())
describe('exhibition dropdown pagination', () => {
  it('applies the published filter on every page for the current exhibition selector', async () => {
    exhibitions.mockResolvedValueOnce({ records: [{ id: 1 }], total: 2 })
    exhibitions.mockResolvedValueOnce({ records: [{ id: 2 }], total: 2 })
    await loadExhibitionOptions({ status: 'PUBLISHED' })
    expect(exhibitions).toHaveBeenNthCalledWith(1, { status: 'PUBLISHED', page: 1, pageSize: 100 })
    expect(exhibitions).toHaveBeenNthCalledWith(2, { status: 'PUBLISHED', page: 2, pageSize: 100 })
  })
  it('loads later pages rather than silently dropping options after 100 records', async () => {
    exhibitions.mockResolvedValueOnce({ records: [{ id: 1 }, { id: 2 }], total: 3 })
    exhibitions.mockResolvedValueOnce({ records: [{ id: 3 }], total: 3 })
    expect(await loadExhibitionOptions()).toEqual([{ id: 1 }, { id: 2 }, { id: 3 }])
    expect(exhibitions).toHaveBeenNthCalledWith(2, { page: 2, pageSize: 100 })
  })
})
