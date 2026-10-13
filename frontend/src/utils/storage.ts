import type { AdminUser } from '@/types/auth'

const TOKEN_KEY = 'eems:access-token'
const USER_KEY = 'eems:admin-user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getAdminUser(): AdminUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as AdminUser
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export function setAdminUser(user: AdminUser): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeAdminUser(): void {
  localStorage.removeItem(USER_KEY)
}
