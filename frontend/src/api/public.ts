import { http } from '@/utils/http'
import type {
  GuestbookCreateRequest,
  PageResult,
  PublicCategory,
  PublicCompany,
  PublicContent,
  PublicContentQuery,
  PublicContentList,
  PublicExhibition,
  PublicExhibitionQuery,
  PublicHome,
} from '@/types/public'

export const publicApi = {
  submitGuestbook(payload: GuestbookCreateRequest): Promise<null> {
    return http.post<null, null>('/api/public/guestbook', payload)
  },

  home(): Promise<PublicHome> {
    return http.get<PublicHome, PublicHome>('/api/public/home')
  },

  company(): Promise<PublicCompany> {
    return http.get<PublicCompany, PublicCompany>('/api/public/company')
  },

  categoryTree(): Promise<PublicCategory[]> {
    return http.get<PublicCategory[], PublicCategory[]>('/api/public/categories/tree')
  },

  exhibitions(params: PublicExhibitionQuery = {}): Promise<PageResult<PublicExhibition>> {
    return http.get<PageResult<PublicExhibition>, PageResult<PublicExhibition>>('/api/public/exhibitions', { params })
  },

  exhibition(id: number): Promise<PublicExhibition> {
    return http.get<PublicExhibition, PublicExhibition>(`/api/public/exhibitions/${id}`)
  },

  categoryContents(code: string, params: PublicContentQuery = {}): Promise<PageResult<PublicContentList>> {
    return http.get<PageResult<PublicContentList>, PageResult<PublicContentList>>(
      `/api/public/categories/${encodeURIComponent(code)}/contents`,
      { params },
    )
  },

  contentDetail(id: number): Promise<PublicContent> {
    return http.get<PublicContent, PublicContent>(`/api/public/contents/${id}`)
  },
}
