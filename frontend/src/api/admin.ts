import { http } from '@/utils/http'
import type { AxiosProgressEvent } from 'axios'
import type {
  AdminExhibition,
  AdminExhibitionPage,
  AdminExhibitionQuery,
  AdminExhibitionSaveRequest,
  AdminFile,
} from '@/types/admin'

export const adminApi = {
  exhibitions(params: AdminExhibitionQuery = {}): Promise<AdminExhibitionPage> {
    return http.get<AdminExhibitionPage, AdminExhibitionPage>('/api/admin/exhibitions', { params })
  },

  exhibition(id: number): Promise<AdminExhibition> {
    return http.get<AdminExhibition, AdminExhibition>(`/api/admin/exhibitions/${id}`)
  },

  createExhibition(payload: AdminExhibitionSaveRequest): Promise<AdminExhibition> {
    return http.post<AdminExhibition, AdminExhibition>('/api/admin/exhibitions', payload)
  },

  updateExhibition(id: number, payload: AdminExhibitionSaveRequest): Promise<AdminExhibition> {
    return http.put<AdminExhibition, AdminExhibition>(`/api/admin/exhibitions/${id}`, payload)
  },

  deleteExhibition(id: number): Promise<void> {
    return http.delete<void, void>(`/api/admin/exhibitions/${id}`)
  },

  publishExhibition(id: number): Promise<AdminExhibition> {
    return http.post<AdminExhibition, AdminExhibition>(`/api/admin/exhibitions/${id}/publish`)
  },

  offlineExhibition(id: number): Promise<AdminExhibition> {
    return http.post<AdminExhibition, AdminExhibition>(`/api/admin/exhibitions/${id}/offline`)
  },

  uploadFile(file: File, onProgress?: (percent: number) => void): Promise<AdminFile> {
    const formData = new FormData()
    formData.append('file', file)
    return http.post<AdminFile, AdminFile>('/api/admin/files', formData, {
      timeout: 10 * 60 * 1000,
      maxContentLength: Infinity,
      maxBodyLength: Infinity,
      onUploadProgress: (event: AxiosProgressEvent) => {
        if (event.total) onProgress?.(Math.min(100, Math.round((event.loaded / event.total) * 100)))
      },
    })
  },
}
