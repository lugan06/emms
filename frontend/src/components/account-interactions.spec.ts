import { afterEach, beforeEach, describe, expect, it, vi, type MockInstance } from 'vitest'
import { createApp, h, nextTick, reactive, type App, type Component } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus, { ElMessage } from 'element-plus'
import PublicGuestbookForm from './public/PublicGuestbookForm.vue'
import PasswordChangeDialog from './admin/PasswordChangeDialog.vue'
import SiteLogo from './public/SiteLogo.vue'
import { useUserStore } from '@/stores/user'

const { submitGuestbook, changePassword, replace } = vi.hoisted(() => ({
  submitGuestbook: vi.fn(), changePassword: vi.fn(), replace: vi.fn(),
}))
vi.mock('@/api/public', () => ({ publicApi: { submitGuestbook } }))
vi.mock('@/api/auth', () => ({ authApi: { changePassword } }))
vi.mock('vue-router', () => ({ useRouter: () => ({ replace }) }))

let app: App
let container: HTMLDivElement
let success: MockInstance<typeof ElMessage.success>

function mount(component: Component, props: Record<string, unknown> = {}) {
  const pinia = createPinia()
  container = document.createElement('div')
  document.body.append(container)
  app = createApp({ render: () => h(component, props) })
  app.use(pinia).use(ElementPlus).mount(container)
  return pinia
}

async function input(name: string, value: string) {
  const node = document.querySelector<HTMLInputElement | HTMLTextAreaElement>(`[name="${name}"]`)!
  node.value = value
  node.dispatchEvent(new Event('input', { bubbles: true }))
  await nextTick()
}

async function submit() {
  await nextTick()
  const node = document.querySelector('form')!
  node.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }))
  await nextTick()
}

beforeEach(() => {
  vi.clearAllMocks()
  success = vi.spyOn(ElMessage, 'success').mockImplementation(() => ({ close: () => {} }))
})
afterEach(() => {
  app?.unmount()
  container?.remove()
  document.body.innerHTML = ''
  vi.restoreAllMocks()
})

describe('public guestbook interactions', () => {
  it('blocks invalid input, preserves a failed submission and clears fields after a successful retry', async () => {
    mount(PublicGuestbookForm)
    await submit()
    await vi.waitFor(() => expect(document.body.textContent).toContain('请输入姓名'))
    expect(submitGuestbook).not.toHaveBeenCalled()
    await input('name', ' 访客 ')
    await input('message', ' 测试咨询 ')
    await input('email', 'invalid')
    await submit()
    await vi.waitFor(() => expect(document.body.textContent).toContain('请输入有效的邮箱地址'))
    expect(submitGuestbook).not.toHaveBeenCalled()
    await input('email', '')
    submitGuestbook.mockRejectedValueOnce(new Error('限流'))
    await submit()
    await vi.waitFor(() => expect(submitGuestbook).toHaveBeenCalledTimes(1))
    await vi.waitFor(() => expect(document.querySelector('button')?.disabled).toBe(false))
    expect(document.querySelector<HTMLTextAreaElement>('textarea')?.value).toBe(' 测试咨询 ')
    expect(success).not.toHaveBeenCalled()
    submitGuestbook.mockResolvedValueOnce(null)
    await submit()
    await vi.waitFor(() => expect(success).toHaveBeenCalledWith('留言提交成功'))
    expect(submitGuestbook).toHaveBeenLastCalledWith({ name: '访客', phone: null, email: null, companyName: null, message: '测试咨询' })
    for (const field of document.querySelectorAll<HTMLInputElement>('input, textarea')) expect(field.value).toBe('')
  })
})

describe('password dialog interactions', () => {
  it('validates confirmation, retains auth on failure, then clears persisted auth and redirects on success', async () => {
    localStorage.setItem('eems:access-token', 'test-token')
    const state = reactive({ modelValue: true, 'onUpdate:modelValue': (value: boolean) => { state.modelValue = value } })
    const pinia = mount(PasswordChangeDialog, state)
    const user = useUserStore(pinia)
    user.user = { id: 1, username: 'admin', nickname: null, avatarUrl: null, roleCode: 'SUPER_ADMIN' }
    await vi.waitFor(() => expect(document.querySelector('[name="oldPassword"]')).not.toBeNull())
    await input('oldPassword', 'old password')
    await input('newPassword', 'new password')
    await input('confirmPassword', 'different')
    await submit()
    await vi.waitFor(() => expect(document.body.textContent).toContain('两次输入的新密码不一致'))
    expect(changePassword).not.toHaveBeenCalled()
    await input('confirmPassword', 'new password')
    changePassword.mockRejectedValueOnce(new Error('原密码错误'))
    await submit()
    await vi.waitFor(() => expect(changePassword).toHaveBeenCalledTimes(1))
    await vi.waitFor(() => expect(document.querySelector<HTMLButtonElement>('button[type="submit"]')?.disabled).toBe(false))
    expect(user.token).toBe('test-token')
    expect(replace).not.toHaveBeenCalled()
    changePassword.mockResolvedValueOnce(null)
    await submit()
    await vi.waitFor(() => expect(replace).toHaveBeenCalledWith({ name: 'admin-login' }))
    expect(changePassword).toHaveBeenLastCalledWith({ oldPassword: 'old password', newPassword: 'new password' })
    expect(user.token).toBeNull()
    expect(user.user).toBeNull()
    expect(localStorage.getItem('eems:access-token')).toBeNull()
    expect(success).toHaveBeenCalledWith('密码修改成功，请重新登录')
    expect(state.modelValue).toBe(false)
    expect(document.querySelector<HTMLInputElement>('[name="newPassword"]')?.value || '').toBe('')
  })
})

describe('site logo fallback', () => {
  it('uses the bundled logo for missing and broken URLs, then retries a changed URL', async () => {
    const props = reactive({ logoUrl: null as string | null, title: '站点名称' })
    mount(SiteLogo, props)
    const img = container.querySelector('img')!
    expect(img.getAttribute('src')).toContain('default-logo.svg')
    expect(img.alt).toBe('站点名称')
    props.logoUrl = '/uploads/missing-logo.png'
    await nextTick()
    expect(img.src).toContain('/uploads/missing-logo.png')
    img.dispatchEvent(new Event('error'))
    await nextTick()
    expect(img.getAttribute('src')).toContain('default-logo.svg')
    props.logoUrl = '/uploads/new-logo.png'
    await nextTick()
    expect(img.src).toContain('/uploads/new-logo.png')
  })
})
