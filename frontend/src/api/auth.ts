import { http } from '@/utils/http'
import type { AdminUser, LoginRequest, LoginResult, PasswordChangeRequest } from '@/types/auth'

export const authApi = {
  changePassword(payload: PasswordChangeRequest): Promise<null> {
    return http.put<null, null>('/api/admin/password', payload)
  },
  me(): Promise<AdminUser> { return http.get<AdminUser, AdminUser>('/api/admin/me') },
  login(payload: LoginRequest): Promise<LoginResult> {
    return http.post<LoginResult, LoginResult>('/api/admin/login', payload)
  },
}
