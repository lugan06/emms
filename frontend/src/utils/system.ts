import type { CompanySaveRequest, SiteSaveRequest, GuestbookStatus, LogQuery } from '@/types/system'
import type { AdminUser } from '@/types/auth'

export const guestbookStatusOptions = [
  { value: 'UNREAD', label: '未读' }, { value: 'READ', label: '已读' },
  { value: 'REPLIED', label: '已回复' }, { value: 'CLOSED', label: '已关闭' },
] as const
export function guestbookStatusLabel(status: GuestbookStatus): string {
  return guestbookStatusOptions.find(item => item.value === status)?.label || status
}
export function isSuperAdmin(user: AdminUser | null): boolean { return user?.roleCode === 'SUPER_ADMIN' }
export function isAdminMenuActive(path: string, target: string): boolean {
  return path === target || (target !== '/admin' && path.startsWith(`${target}/`))
}
export function createSiteForm(): SiteSaveRequest {
  return { siteTitle: '', siteSubtitle: null, domain: null, logoUrl: null, keywords: null, description: null,
    icpNumber: null, templateCode: null, statisticsCode: null, footerInfo: null, currentExhibitionId: null,
    heroVideoUrl: null }
}
export function createCompanyForm(): CompanySaveRequest {
  return { companyName: '', address: null, postalCode: null, contactName: null, mobile: null,
    telephone: null, fax: null, email: null, qq: null, wechatImageUrl: null, businessLicenseNo: null, otherInfo: null }
}
export function validateSiteForm(form: SiteSaveRequest, publishedIds: number[]): true | string {
  if (!form.siteTitle.trim()) return '请输入站点标题'
  if (form.siteTitle.length > 200) return '站点标题不能超过 200 字符'
  if (form.currentExhibitionId !== null && !publishedIds.includes(form.currentExhibitionId)) return '当前展会必须选择已发布的展会，或清空选择'
  return true
}
export function validateCompanyForm(form: CompanySaveRequest): true | string {
  if (!form.companyName.trim()) return '请输入公司名称'
  if (form.companyName.length > 200) return '公司名称不能超过 200 字符'
  if (form.email?.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) return '请输入有效的邮箱地址'
  return true
}
export function validateReply(replyContent: string): true | string {
  if (!replyContent.trim()) return '请输入回复内容'
  if (replyContent.length > 2000) return '回复内容不能超过 2000 字符'
  return true
}
export function validateLogRange(query: Pick<LogQuery, 'startAt' | 'endAt'>): true | string {
  if (query.startAt && query.endAt && query.startAt > query.endAt) return '开始时间不能晚于结束时间'
  return true
}
