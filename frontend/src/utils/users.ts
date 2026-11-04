import type { AdminUserCreate } from '@/types/users'
import type { SiteOption } from '@/types/sites'

export interface AdminUserForm extends AdminUserCreate { confirmPassword: string }
export function createAdminUserForm(): AdminUserForm {
  return { username: '', password: '', confirmPassword: '', nickname: '', siteIds: [] }
}
export function validateAdminNickname(nickname: string): true | string {
  return nickname.trim().length > 0 && nickname.trim().length <= 100 ? true : '请输入 1 至 100 字的昵称'
}
export function validateAdminPassword(password: string, confirmation: string): true | string {
  if (!password.trim() || password.length < 8 || new TextEncoder().encode(password).length > 72) {
    return '密码至少 8 字符且 UTF-8 不超过 72 字节'
  }
  if (password !== confirmation) return '两次输入的密码不一致'
  return true
}
export function validateAdminSites(ids: number[], options: SiteOption[]): true | string {
  if (ids.length > 1000 || new Set(ids).size !== ids.length ||
      ids.some(id => !Number.isSafeInteger(id) || id <= 0 || !options.some(site => site.id === id))) {
    return '请选择有效且不重复的授权站点'
  }
  return true
}
export function validateAdminCreate(form: AdminUserForm, options: SiteOption[]): true | string {
  if (!/^[A-Za-z0-9][A-Za-z0-9_.@-]{0,49}$/.test(form.username.trim())) return '用户名须为 1 至 50 位字母、数字、下划线、点、@ 或连字符，且以字母或数字开头'
  const nickname = validateAdminNickname(form.nickname)
  if (nickname !== true) return nickname
  const password = validateAdminPassword(form.password, form.confirmPassword)
  return password === true ? validateAdminSites(form.siteIds, options) : password
}
export function adminCreatePayload(form: AdminUserForm): AdminUserCreate {
  return { username: form.username.trim(), nickname: form.nickname.trim(), password: form.password, siteIds: [...form.siteIds] }
}

