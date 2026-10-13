import type { DateTimeString, PageResult } from './public'

export type ExhibitionStatus = 'DRAFT' | 'PUBLISHED' | 'OFFLINE'

export interface AdminExhibition {
  id: number
  exhibitionCode: string
  title: string
  subtitle: string | null
  year: number
  edition: string | null
  coverUrl: string | null
  summary: string | null
  description: string | null
  venue: string | null
  address: string | null
  startAt: DateTimeString
  endAt: DateTimeString
  status: ExhibitionStatus
  showOnHome: number
  homeSort: number
  publishedAt: DateTimeString | null
  contactName: string | null
  contactPhone: string | null
  registrationUrl: string | null
  extraData: string | null
  createdAt: DateTimeString
  updatedAt: DateTimeString
}

export interface AdminExhibitionQuery {
  keyword?: string
  status?: ExhibitionStatus
  year?: number
  page?: number
  pageSize?: number
}

export interface AdminExhibitionSaveRequest {
  exhibitionCode: string
  title: string
  subtitle: string | null
  year: number | undefined
  edition: string | null
  coverUrl: string | null
  summary: string | null
  description: string | null
  venue: string | null
  address: string | null
  startAt: string
  endAt: string
  registrationUrl: string | null
  contactName: string | null
  contactPhone: string | null
  showOnHome: number
  homeSort: number
  extraData: string | null
}

export type AdminExhibitionPage = PageResult<AdminExhibition>

export interface AdminFile {
  id: number
  originalName: string
  fileUrl: string
  fileType: string
  fileSize: number
  uploadedBy: number | null
  createdAt: DateTimeString
}

export type CategoryModel = 'PAGE' | 'ARTICLE' | 'PRODUCT' | 'CASE' | 'LINK' | 'DIRECTORY'
export type BinaryFlag = 0 | 1

export interface CategorySaveRequest {
  parentId: number
  name: string
  code: string
  urlName: string | null
  modelCode: CategoryModel
  listTemplate: string | null
  detailTemplate: string | null
  sortOrder: number
  isEnabled: BinaryFlag
  showInNav: BinaryFlag
  linkUrl: string | null
}

export interface AdminCategory extends CategorySaveRequest {
  id: number
  createdAt: DateTimeString
  updatedAt: DateTimeString
  children: AdminCategory[]
}

export interface CategoryReorderRequest {
  items: Array<{ id: number; parentId: number; sortOrder: number }>
}

export interface ContentSaveRequest {
  categoryId: number | null
  exhibitionId: number | null
  title: string
  slug: string | null
  coverUrl: string | null
  summary: string | null
  body: string | null
  author: string | null
  source: string | null
  sortOrder: number
  isTop: BinaryFlag
  isRecommend: BinaryFlag
  extraData: string | null
}

export interface AdminContent extends Omit<ContentSaveRequest, 'categoryId'> {
  id: number
  categoryId: number
  status: ExhibitionStatus
  publishedAt: DateTimeString | null
  viewCount: number
  createdAt: DateTimeString
  updatedAt: DateTimeString
}

export interface ContentPageQuery {
  categoryId?: number
  exhibitionId?: number
  keyword?: string
  status?: ExhibitionStatus
  isTop?: BinaryFlag
  isRecommend?: BinaryFlag
  page?: number
  pageSize?: number
}

export type AdminContentPage = PageResult<AdminContent>
