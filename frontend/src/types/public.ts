export type DateTimeString = string

export interface PublicSite {
  siteTitle: string | null
  siteSubtitle: string | null
  domain: string | null
  logoUrl: string | null
  keywords: string | null
  description: string | null
  icpNumber: string | null
  footerInfo: string | null
}

export interface PublicCompany {
  companyName: string | null
  address: string | null
  postalCode: string | null
  contactName: string | null
  telephone: string | null
  email: string | null
  wechatImageUrl: string | null
}

export interface PublicContentList {
  id: number
  categoryId: number
  categoryCode: string
  categoryName: string
  exhibitionId: number | null
  title: string
  slug: string | null
  coverUrl: string | null
  summary: string | null
  author: string | null
  source: string | null
  publishedAt: DateTimeString | null
  isTop: number
  isRecommend: number
}

export interface PublicExhibition {
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
  publishedAt: DateTimeString | null
  contactName: string | null
  contactPhone: string | null
  registrationUrl: string | null
  showOnHome: number
  homeSort: number
  isCurrent: boolean
}

export interface PublicHome {
  site: PublicSite
  company: PublicCompany
  currentExhibition: PublicExhibition | null
  exhibitionIntro: PublicContentList[]
  samePeriodActivities: PublicContentList[]
  exhibitionScope: PublicContentList[]
  exhibitionNews: PublicContentList[]
  industryNews: PublicContentList[]
  exhibitorNews: PublicContentList[]
}

export interface PublicCategory {
  id: number
  parentId: number
  name: string
  code: string
  urlName: string | null
  modelCode: string
  linkUrl: string | null
  children: PublicCategory[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface PublicExhibitionQuery {
  keyword?: string
  year?: number
  page?: number
  pageSize?: number
}
