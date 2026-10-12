import { http } from '@/utils/http'
import type {
  PageResult,
  PublicCategory,
  PublicContent,
  PublicContentQuery,
  PublicContentList,
  PublicExhibition,
  PublicExhibitionQuery,
  PublicHome,
} from '@/types/public'

export const publicApi = {
  home(): Promise<PublicHome> {
    return http.get<PublicHome, PublicHome>('/api/public/home')
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
