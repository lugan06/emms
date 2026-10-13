import type { PasswordChangeRequest } from '@/types/auth'
import type { GuestbookCreateRequest } from '@/types/public'

export interface GuestbookForm {
  name: string
  phone: string
  email: string
  companyName: string
  message: string
}

export interface PasswordChangeForm extends PasswordChangeRequest {
  confirmPassword: string
}

export function createGuestbookForm(): GuestbookForm {
  return { name: '', phone: '', email: '', companyName: '', message: '' }
}

export function createPasswordForm(): PasswordChangeForm {
  return { oldPassword: '', newPassword: '', confirmPassword: '' }
}

export function guestbookPayload(form: GuestbookForm): GuestbookCreateRequest {
  return {
    name: form.name.trim(),
    phone: form.phone.trim() || null,
    email: form.email.trim() || null,
    companyName: form.companyName.trim() || null,
    message: form.message.trim(),
  }
}

export function guestbookErrors(form: GuestbookForm): Partial<Record<keyof GuestbookForm, string>> {
  const data = guestbookPayload(form)
  const errors: Partial<Record<keyof GuestbookForm, string>> = {}
  if (!data.name) errors.name = '请输入姓名'
  else if (data.name.length > 100) errors.name = '姓名不能超过 100 字'
  if (!data.message) errors.message = '请输入留言内容'
  else if (data.message.length > 2000) errors.message = '留言内容不能超过 2000 字'
  if (data.phone && !/^[0-9+()\-\s]{6,30}$/.test(data.phone)) errors.phone = '请输入有效的联系电话（6–30 位）'
  if (data.email && (data.email.length > 150 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email))) {
    errors.email = '请输入有效的邮箱地址'
  }
  if (data.companyName && data.companyName.length > 200) errors.companyName = '公司名称不能超过 200 字'
  return errors
}

export function passwordErrors(form: PasswordChangeForm): Partial<Record<keyof PasswordChangeForm, string>> {
  const errors: Partial<Record<keyof PasswordChangeForm, string>> = {}
  if (!form.oldPassword.trim()) errors.oldPassword = '请输入原密码'
  if (!form.newPassword.trim() || form.newPassword.length < 8 || form.newPassword.length > 100) {
    errors.newPassword = '新密码长度必须为 8–100 位'
  }
  if (!form.confirmPassword) errors.confirmPassword = '请再次输入新密码'
  else if (form.confirmPassword !== form.newPassword) errors.confirmPassword = '两次输入的新密码不一致'
  return errors
}
