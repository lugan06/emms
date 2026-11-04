import type { PageResult } from './public'
import type { SiteOption, SiteStatus } from './sites'

export interface ManagedAdminUser {
  id: number
  username: string
  nickname: string
  status: SiteStatus
  siteIds: number[]
  sites: SiteOption[]
  createdAt: string
  updatedAt: string
}
export interface AdminUserCreate {
  username: string
  password: string
  nickname: string
  siteIds: number[]
}
export interface AdminUserQuery { keyword?: string; status?: SiteStatus; siteId?: number; page?: number; pageSize?: number }
export type AdminUserPage = PageResult<ManagedAdminUser>

