import { http } from '@/utils/http'
import type { LoginRequest, LoginResult } from '@/types/auth'

export const authApi = {
  login(payload: LoginRequest): Promise<LoginResult> {
    return http.post<LoginResult, LoginResult>('/api/admin/login', payload)
  },
}
