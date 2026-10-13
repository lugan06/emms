import { describe, expect, it, vi } from 'vitest'
const { exhibitions } = vi.hoisted(() => ({ exhibitions: vi.fn() }))
vi.mock('@/api/admin', () => ({ adminApi: { exhibitions } }))
import { loadExhibitionOptions } from './admin-options'
describe('exhibition dropdown pagination', () => {
  it('loads later pages rather than silently dropping options after 100 records', async () => {
    exhibitions.mockResolvedValueOnce({ records: [{ id: 1 }, { id: 2 }], total: 3 })
    exhibitions.mockResolvedValueOnce({ records: [{ id: 3 }], total: 3 })
    expect(await loadExhibitionOptions()).toEqual([{ id: 1 }, { id: 2 }, { id: 3 }])
    expect(exhibitions).toHaveBeenNthCalledWith(2, { page: 2, pageSize: 100 })
  })
})
