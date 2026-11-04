import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, nextTick, type App } from 'vue'
import ElementPlus, { ElMessage, ElMessageBox } from 'element-plus'
import AdminUserManagementView from './AdminUserManagementView.vue'
import type { ManagedAdminUser } from '@/types/users'
const { page, create, update, sites, status, password, options } = vi.hoisted(() => ({
  page: vi.fn(), create: vi.fn(), update: vi.fn(), sites: vi.fn(), status: vi.fn(), password: vi.fn(), options: vi.fn(),
}))
vi.mock('@/api/users', () => ({ usersApi: { page, create, update, sites, status, password } }))
vi.mock('@/api/sites', () => ({ sitesApi: { options } }))

const row: ManagedAdminUser = { id: 7, username: 'editor', nickname: 'Editor', status: 'ENABLED', siteIds: [1],
  sites: [{ id: 1, name: 'Site A', code: 'a', status: 'ENABLED' }], createdAt: '', updatedAt: '' }
let app: App | undefined
let container: HTMLDivElement
function mount() {
  container = document.createElement('div'); document.body.append(container)
  app = createApp({ render: () => h(AdminUserManagementView) }); app.use(ElementPlus).mount(container)
}
async function click(selector: string) {
  document.querySelector<HTMLButtonElement>(selector)!.click(); await nextTick()
}
async function input(name: string, value: string) {
  const node = document.querySelector<HTMLInputElement>(`[name="${name}"]`)!
  node.value = value; node.dispatchEvent(new Event('input', { bubbles: true })); await nextTick()
}
async function button(text: string) {
  const node = [...document.querySelectorAll<HTMLButtonElement>('button')].find(item => item.textContent?.trim() === text)!
  expect(node).toBeDefined(); node.click(); await nextTick()
}
async function dialog(selector: string) {
  await vi.waitFor(() => expect(document.querySelector(selector)).not.toBeNull())
  await click(selector)
  await vi.waitFor(() => expect(document.querySelector('[name="adminUsername"]')).not.toBeNull())
  await vi.waitFor(() => expect(document.querySelector<HTMLButtonElement>('[data-testid="save-user"]')!.disabled).toBe(false))
}
beforeEach(() => {
  vi.clearAllMocks()
  page.mockResolvedValue({ records: [structuredClone(row)], total: 1, page: 1, pageSize: 20 })
  options.mockResolvedValue([{ id: 1, name: 'Site A', code: 'a', status: 'ENABLED' }, { id: 2, name: 'Site B', code: 'b', status: 'DISABLED' }])
  vi.spyOn(ElMessage, 'success').mockImplementation(() => ({ close: () => {} }))
})
afterEach(() => { app?.unmount(); app = undefined; container?.remove(); document.body.innerHTML = ''; vi.restoreAllMocks() })

describe('administrator management page', () => {
  it('validates creation, preserves failed drafts and sends only account fields', async () => {
    mount(); await dialog('[data-testid="create-user"]')
    await click('[data-testid="save-user"]')
    expect(create).not.toHaveBeenCalled()
    await input('adminUsername', ' editor-new '); await input('adminNickname', ' New editor ')
    await input('adminPassword', ' secure-password '); await input('adminConfirmPassword', 'wrong')
    await click('[data-testid="save-user"]')
    expect(create).not.toHaveBeenCalled(); expect(document.body.textContent).toContain('两次输入的密码不一致')
    await input('adminConfirmPassword', ' secure-password ')
    create.mockRejectedValueOnce(new Error('duplicate'))
    await click('[data-testid="save-user"]')
    await vi.waitFor(() => expect(create).toHaveBeenCalledOnce())
    await vi.waitFor(() => expect(document.querySelector<HTMLButtonElement>('[data-testid="save-user"]')!.disabled).toBe(false))
    expect(document.querySelector<HTMLInputElement>('[name="adminNickname"]')!.value).toBe(' New editor ')
    create.mockResolvedValueOnce(row)
    await click('[data-testid="save-user"]')
    await vi.waitFor(() => expect(ElMessage.success).toHaveBeenCalledWith('管理员信息已保存'))
    expect(create).toHaveBeenLastCalledWith({ username: 'editor-new', nickname: 'New editor', password: ' secure-password ', siteIds: [] })
    expect(document.querySelector<HTMLInputElement>('[name="adminPassword"]')?.value || '').toBe('')
  })

  it('edits only nickname and keeps the username immutable', async () => {
    mount(); await dialog('[data-edit-user="7"]')
    expect(document.querySelector<HTMLInputElement>('[name="adminUsername"]')!.disabled).toBe(true)
    expect(document.querySelector('[name="adminPassword"]')).toBeNull()
    await input('adminNickname', 'Renamed')
    await click('[data-testid="save-user"]')
    await vi.waitFor(() => expect(update).toHaveBeenCalledWith(7, 'Renamed'))
    expect(sites).not.toHaveBeenCalled()
  })

  it('uses the grant endpoint independently and can revoke all sites', async () => {
    mount(); await dialog('[data-grant-user="7"]')
    const remove = document.querySelector<HTMLElement>('.site-grant-select .el-tag__close')!
    expect(remove).not.toBeNull(); remove.click(); await nextTick()
    await click('[data-testid="save-user"]')
    await vi.waitFor(() => expect(sites).toHaveBeenCalledWith(7, []))
    expect(row.siteIds).toEqual([1]); expect(update).not.toHaveBeenCalled()
  })

  it('blocks grant saving while options are unavailable and supports retry', async () => {
    options.mockRejectedValue(new Error('offline'))
    mount()
    await vi.waitFor(() => expect(document.body.textContent).toContain('站点选项加载失败'))
    await click('[data-grant-user="7"]')
    await vi.waitFor(() => expect(document.querySelector('[data-testid="save-user"]')).not.toBeNull())
    expect(document.querySelector<HTMLButtonElement>('[data-testid="save-user"]')!.disabled).toBe(true)
    expect(sites).not.toHaveBeenCalled()
    options.mockResolvedValue([{ id: 1, name: 'Site A', code: 'a', status: 'ENABLED' }])
    await button('重试加载站点')
    await vi.waitFor(() => expect(document.querySelector<HTMLButtonElement>('[data-testid="save-user"]')!.disabled).toBe(false))
    await click('[data-testid="save-user"]')
    await vi.waitFor(() => expect(sites).toHaveBeenCalledWith(7, [1]))
  })

  it('requires password confirmation and retains retry input on failure', async () => {
    mount(); await dialog('[data-reset-user="7"]')
    await input('adminPassword', 'replacement-password'); await input('adminConfirmPassword', 'mismatch')
    await click('[data-testid="save-user"]')
    expect(password).not.toHaveBeenCalled()
    await input('adminConfirmPassword', 'replacement-password')
    password.mockRejectedValueOnce(new Error('failed'))
    await click('[data-testid="save-user"]')
    await vi.waitFor(() => expect(password).toHaveBeenCalledOnce())
    await vi.waitFor(() => expect(document.querySelector<HTMLButtonElement>('[data-testid="save-user"]')!.disabled).toBe(false))
    password.mockResolvedValueOnce(undefined)
    await click('[data-testid="save-user"]')
    await vi.waitFor(() => expect(ElMessage.success).toHaveBeenCalledWith('密码已重置，该管理员需重新登录'))
    expect(password).toHaveBeenLastCalledWith(7, 'replacement-password')
  })

  it('confirms disable, respects cancellation, and reloads after success', async () => {
    const confirm = vi.spyOn(ElMessageBox, 'confirm').mockRejectedValueOnce('cancel')
    mount()
    await vi.waitFor(() => expect(document.querySelector('[data-status-user="7"]')).not.toBeNull())
    await click('[data-status-user="7"]')
    await vi.waitFor(() => expect(confirm).toHaveBeenCalledOnce())
    expect(status).not.toHaveBeenCalled()
    await vi.waitFor(() => expect(document.querySelector<HTMLButtonElement>('[data-status-user="7"]')!.disabled).toBe(false))
    confirm.mockImplementationOnce(() => Promise.resolve('confirm') as ReturnType<typeof ElMessageBox.confirm>)
    await click('[data-status-user="7"]')
    await vi.waitFor(() => expect(status).toHaveBeenCalledWith(7, 'DISABLED'))
    await vi.waitFor(() => expect(page).toHaveBeenCalledTimes(2))
  })

  it('shows load errors and forwards search and reset filters', async () => {
    page.mockRejectedValueOnce(new Error('offline'))
    mount()
    await vi.waitFor(() => expect(document.body.textContent).toContain('管理员加载失败'))
    await button('重新加载')
    await vi.waitFor(() => expect(document.querySelector('[data-edit-user="7"]')).not.toBeNull())
    await input('userKeyword', ' editor ')
    await button('搜索')
    await vi.waitFor(() => expect(page).toHaveBeenLastCalledWith({ keyword: 'editor', status: undefined, siteId: undefined, page: 1, pageSize: 20 }))
    await vi.waitFor(() => expect([...document.querySelectorAll<HTMLButtonElement>('button')].find(b => b.textContent?.trim() === '重置')!.disabled).toBe(false))
    await button('重置')
    await vi.waitFor(() => expect(page).toHaveBeenLastCalledWith({ keyword: undefined, status: undefined, siteId: undefined, page: 1, pageSize: 20 }))
  })
})

