import { http } from '@/utils/http'
import type { AdminCategory, AdminContent, AdminContentPage, CategorySaveRequest, CategoryReorderRequest, ContentSaveRequest, ContentPageQuery } from '@/types/admin'

export const cmsApi = {
  categoryTree(): Promise<AdminCategory[]> {
    return http.get<AdminCategory[], AdminCategory[]>('/api/admin/categories/tree')
  },
  createCategory(payload: CategorySaveRequest): Promise<AdminCategory> {
    return http.post<AdminCategory, AdminCategory>('/api/admin/categories', payload)
  },
  updateCategory(id: number, payload: CategorySaveRequest): Promise<AdminCategory> {
    return http.put<AdminCategory, AdminCategory>(`/api/admin/categories/${id}`, payload)
  },
  deleteCategory(id: number): Promise<AdminCategory> {
    return http.delete<AdminCategory, AdminCategory>(`/api/admin/categories/${id}`)
  },
  reorderCategories(payload: CategoryReorderRequest): Promise<void> {
    return http.post<void, void>('/api/admin/categories/reorder', payload)
  },
  contents(params: ContentPageQuery = {}): Promise<AdminContentPage> {
    return http.get<AdminContentPage, AdminContentPage>('/api/admin/contents', { params })
  },
  content(id: number): Promise<AdminContent> {
    return http.get<AdminContent, AdminContent>(`/api/admin/contents/${id}`)
  },
  createContent(payload: ContentSaveRequest): Promise<AdminContent> {
    return http.post<AdminContent, AdminContent>('/api/admin/contents', payload)
  },
  updateContent(id: number, payload: ContentSaveRequest): Promise<AdminContent> {
    return http.put<AdminContent, AdminContent>(`/api/admin/contents/${id}`, payload)
  },
  deleteContent(id: number): Promise<void> {
    return http.delete<void, void>(`/api/admin/contents/${id}`)
  },
  publishContent(id: number): Promise<AdminContent> {
    return http.post<AdminContent, AdminContent>(`/api/admin/contents/${id}/publish`)
  },
  offlineContent(id: number): Promise<AdminContent> {
    return http.post<AdminContent, AdminContent>(`/api/admin/contents/${id}/offline`)
  },
}
