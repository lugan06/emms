import type { PageResult } from './public'

export type SiteStatus = 'ENABLED' | 'DISABLED'
export interface SiteOption { id: number; name: string; code: string; status: SiteStatus }
export interface SiteDomain { domain: string; primary: boolean }
export interface SiteManagementSave { name: string; code: string; domains: SiteDomain[] }
export interface ManagedSite extends SiteManagementSave {
  id: number
  status: SiteStatus
  createdAt: string
  updatedAt: string
}
export interface SiteQuery { keyword?: string; status?: SiteStatus; page?: number; pageSize?: number }
export type SitePage = PageResult<ManagedSite>

