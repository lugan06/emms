import { http } from '@/utils/http'
import type { AdminCompany, AdminSite, AdminGuestbook, CompanySaveRequest, SiteSaveRequest,
  GuestbookPage, GuestbookQuery, GuestbookReplyRequest, LogPage, LogQuery } from '@/types/system'

export const systemApi = {
  site(): Promise<AdminSite> { return http.get<AdminSite, AdminSite>('/api/admin/site') },
  updateSite(payload: SiteSaveRequest): Promise<AdminSite> { return http.put<AdminSite, AdminSite>('/api/admin/site', payload) },
  company(): Promise<AdminCompany> { return http.get<AdminCompany, AdminCompany>('/api/admin/company') },
  updateCompany(payload: CompanySaveRequest): Promise<AdminCompany> { return http.put<AdminCompany, AdminCompany>('/api/admin/company', payload) },
  guestbooks(params: GuestbookQuery = {}): Promise<GuestbookPage> { return http.get<GuestbookPage, GuestbookPage>('/api/admin/guestbooks', { params }) },
  readGuestbook(id: number): Promise<AdminGuestbook> { return http.post<AdminGuestbook, AdminGuestbook>(`/api/admin/guestbooks/${id}/read`) },
  replyGuestbook(id: number, payload: GuestbookReplyRequest): Promise<AdminGuestbook> { return http.post<AdminGuestbook, AdminGuestbook>(`/api/admin/guestbooks/${id}/reply`, payload) },
  closeGuestbook(id: number): Promise<AdminGuestbook> { return http.post<AdminGuestbook, AdminGuestbook>(`/api/admin/guestbooks/${id}/close`) },
  logs(params: LogQuery = {}): Promise<LogPage> { return http.get<LogPage, LogPage>('/api/admin/logs', { params }) },
}
