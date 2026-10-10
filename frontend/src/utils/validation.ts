import type { LoginRequest } from '@/types/auth'

export function isLoginFormValid(form: LoginRequest): boolean {
  return form.username.trim().length > 0 && form.password.length > 0
}
