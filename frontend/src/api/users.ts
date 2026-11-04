import { http } from '@/utils/http'
import type { AdminUserCreate, AdminUserPage, AdminUserQuery, ManagedAdminUser } from '@/types/users'
import type { SiteStatus } from '@/types/sites'

export const usersApi = {
  page(params: AdminUserQuery = {}): Promise<AdminUserPage> {
    return http.get<AdminUserPage, AdminUserPage>('/api/admin/users', { params })
  },
  create(payload: AdminUserCreate): Promise<ManagedAdminUser> {
    return http.post<ManagedAdminUser, ManagedAdminUser>('/api/admin/users', payload)
  },
  update(id: number, nickname: string): Promise<ManagedAdminUser> {
    return http.put<ManagedAdminUser, ManagedAdminUser>(`/api/admin/users/${id}`, { nickname })
  },
  sites(id: number, siteIds: number[]): Promise<ManagedAdminUser> {
    return http.put<ManagedAdminUser, ManagedAdminUser>(`/api/admin/users/${id}/sites`, { siteIds })
  },
  status(id: number, status: SiteStatus): Promise<ManagedAdminUser> {
    return http.put<ManagedAdminUser, ManagedAdminUser>(`/api/admin/users/${id}/status`, { status })
  },
  password(id: number, password: string): Promise<void> {
    return http.put<void, void>(`/api/admin/users/${id}/password`, { password })
  },
}

