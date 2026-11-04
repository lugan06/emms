import { http } from '@/utils/http'
import type { ManagedSite, SiteManagementSave, SitePage, SiteQuery, SiteStatus, SiteOption } from '@/types/sites'

export const sitesApi = {
  options(): Promise<SiteOption[]> {
    return http.get<SiteOption[], SiteOption[]>('/api/admin/sites/options')
  },
  page(params: SiteQuery = {}): Promise<SitePage> {
    return http.get<SitePage, SitePage>('/api/admin/sites', { params })
  },
  create(payload: SiteManagementSave): Promise<ManagedSite> {
    return http.post<ManagedSite, ManagedSite>('/api/admin/sites', payload)
  },
  update(id: number, payload: SiteManagementSave): Promise<ManagedSite> {
    return http.put<ManagedSite, ManagedSite>(`/api/admin/sites/${id}`, payload)
  },
  status(id: number, status: SiteStatus): Promise<ManagedSite> {
    return http.put<ManagedSite, ManagedSite>(`/api/admin/sites/${id}/status`, { status })
  },
}

