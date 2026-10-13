import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createCategoryForm, createContentForm } from '@/utils/cms'

const { get, post, put, del } = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn(), put: vi.fn(), del: vi.fn() }))
vi.mock('@/utils/http', () => ({ http: { get, post, put, delete: del } }))
import { cmsApi } from './cms'

beforeEach(() => vi.clearAllMocks())
describe('cmsApi contract', () => {
  it('uses the category tree and documented mutation routes and reorder envelope', async () => {
    const payload = createCategoryForm()
    const reorder = { items: [{ id: 2, parentId: 0, sortOrder: 12 }] }
    await cmsApi.categoryTree()
    await cmsApi.createCategory(payload)
    await cmsApi.updateCategory(2, payload)
    await cmsApi.reorderCategories(reorder)
    await cmsApi.deleteCategory(2)
    expect(get).toHaveBeenCalledWith('/api/admin/categories/tree')
    expect(post).toHaveBeenCalledWith('/api/admin/categories', payload)
    expect(put).toHaveBeenCalledWith('/api/admin/categories/2', payload)
    expect(post).toHaveBeenCalledWith('/api/admin/categories/reorder', reorder)
    expect(del).toHaveBeenCalledWith('/api/admin/categories/2')
  })
  it('passes content filters and preserves backend field names in saves', async () => {
    const query = { categoryId: 2, exhibitionId: 3, keyword: '内容', status: 'DRAFT' as const, page: 2, pageSize: 10 }
    const payload = { ...createContentForm(), categoryId: 2, title: '文章', body: '<p>正文</p>' }
    await cmsApi.contents(query)
    await cmsApi.content(5)
    await cmsApi.createContent(payload)
    await cmsApi.updateContent(5, payload)
    await cmsApi.publishContent(5)
    await cmsApi.offlineContent(5)
    await cmsApi.deleteContent(5)
    expect(get).toHaveBeenCalledWith('/api/admin/contents', { params: query })
    expect(get).toHaveBeenCalledWith('/api/admin/contents/5')
    expect(post).toHaveBeenCalledWith('/api/admin/contents', payload)
    expect(put).toHaveBeenCalledWith('/api/admin/contents/5', payload)
    expect(post).toHaveBeenCalledWith('/api/admin/contents/5/publish')
    expect(post).toHaveBeenCalledWith('/api/admin/contents/5/offline')
    expect(del).toHaveBeenCalledWith('/api/admin/contents/5')
  })
  it('propagates refused category deletions so callers keep their tree', async () => {
    del.mockRejectedValueOnce(new Error('栏目下有内容，不能删除'))
    await expect(cmsApi.deleteCategory(2)).rejects.toThrow('栏目下有内容，不能删除')
  })
})
