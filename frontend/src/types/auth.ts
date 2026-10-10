export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  user: AdminUser
}

export interface AdminUser {
  id: number
  username: string
  nickname: string | null
  avatarUrl: string | null
  roleCode: string
}
