import { http } from '@/utils/http'
import type {
  PageResult,
  PublicCategory,
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
}
