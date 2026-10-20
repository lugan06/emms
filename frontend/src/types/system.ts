import type { DateTimeString, PageResult } from './public'

export interface SiteSaveRequest {
  siteTitle: string
  siteSubtitle: string | null
  domain: string | null
  logoUrl: string | null
  keywords: string | null
  description: string | null
  icpNumber: string | null
  templateCode: string | null
  statisticsCode: string | null
  footerInfo: string | null
  currentExhibitionId: number | null
  heroVideoUrl: string | null
}
export interface AdminSite extends SiteSaveRequest { id: number; configCode: string }
export interface CompanySaveRequest {
  companyName: string
  address: string | null
  postalCode: string | null
  contactName: string | null
  mobile: string | null
  telephone: string | null
  fax: string | null
  email: string | null
  qq: string | null
  wechatImageUrl: string | null
  businessLicenseNo: string | null
  otherInfo: string | null
}
export interface AdminCompany extends CompanySaveRequest { id: number; profileCode: string }
export type GuestbookStatus = 'UNREAD' | 'READ' | 'REPLIED' | 'CLOSED'
export interface AdminGuestbook {
  id: number
  name: string
  phone: string | null
  email: string | null
  companyName: string | null
  message: string
  status: GuestbookStatus
  replyContent: string | null
  repliedBy: number | null
  repliedAt: DateTimeString | null
  ipAddress: string | null
  createdAt: DateTimeString
  updatedAt: DateTimeString
}
export interface GuestbookQuery { status?: GuestbookStatus; page?: number; pageSize?: number }
export interface GuestbookReplyRequest { replyContent: string }
export type OperationResult = 'SUCCESS' | 'FAILURE'
export interface OperationLog {
  id: number
  adminUserId: number | null
  username: string | null
  module: string
  operation: string
  requestMethod: string
  requestUrl: string
  requestIp: string | null
  requestParams: string | null
  result: OperationResult
  errorMessage: string | null
  createdAt: DateTimeString
}
export interface LogQuery {
  username?: string
  module?: string
  operation?: string
  result?: OperationResult
  startAt?: string
  endAt?: string
  page?: number
  pageSize?: number
}
export type GuestbookPage = PageResult<AdminGuestbook>
export type LogPage = PageResult<OperationLog>
