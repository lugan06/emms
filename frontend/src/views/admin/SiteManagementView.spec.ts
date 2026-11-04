import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, nextTick, type App } from 'vue'
import ElementPlus, { ElMessage, ElMessageBox } from 'element-plus'
import SiteManagementView from './SiteManagementView.vue'
import type { ManagedSite } from '@/types/sites'

const { page, create, update, status } = vi.hoisted(() => ({ page: vi.fn(), create: vi.fn(), update: vi.fn(), status: vi.fn() }))
vi.mock('@/api/sites', () => ({ sitesApi: { page, create, update, status } }))
let app: App | undefined
let container: HTMLDivElement
const row: ManagedSite = { id: 7, name: 'Existing', code: 'existing', status: 'DISABLED',
  createdAt: '2026-11-04 00:00:00', updatedAt: '2026-11-04 00:00:00',
  domains: [{ domain: 'existing.example', primary: true }, { domain: 'alias.example', primary: false }] }

function mount() {
  container = document.createElement('div')
  document.body.append(container)
  app = createApp({ render: () => h(SiteManagementView) })
  app.use(ElementPlus).mount(container)
}
async function click(selector: string) {
  document.querySelector<HTMLButtonElement>(selector)!.click()
  await nextTick()
}
async function input(name: string, value: string) {
  const node = document.querySelector<HTMLInputElement>(`[name="${name}"]`)!
  node.value = value
  node.dispatchEvent(new Event('input', { bubbles: true }))
  await nextTick()
}
async function button(text: string) {
  const node = [...document.querySelectorAll<HTMLButtonElement>('button')].find(b => b.textContent?.trim() === text)!
  expect(node).toBeDefined()
  node.click()
  await nextTick()
}

beforeEach(() => {
  vi.clearAllMocks()
  page.mockResolvedValue({ records: [structuredClone(row)], total: 1, page: 1, pageSize: 20 })
  vi.spyOn(ElMessage, 'success').mockImplementation(() => ({ close: () => {} }))
})
afterEach(() => {
  app?.unmount(); app = undefined
  container?.remove()
  document.body.innerHTML = ''
  vi.restoreAllMocks()
})

describe('site management interactions', () => {
  it('validates create, keeps failed draft and sends a normalized payload on retry', async () => {
    mount()
    await vi.waitFor(() => expect(page).toHaveBeenCalledOnce())
    await click('[data-testid="create-site"]')
    await vi.waitFor(() => expect(document.querySelector('[name="siteName"]')).not.toBeNull())
    await click('[data-testid="save-site"]')
    expect(create).not.toHaveBeenCalled()
    expect(document.body.textContent).toContain('请输入 1 至 200 字的站点名称')
    await input('siteName', ' New site ')
    await input('siteCode', 'new-site')
    await input('siteDomain-0', 'https://wrong.example')
    await click('[data-testid="save-site"]')
    expect(create).not.toHaveBeenCalled()
    expect(document.body.textContent).toContain('域名格式无效')
    await input('siteDomain-0', ' NEW.EXAMPLE ')
    create.mockRejectedValueOnce(new Error('duplicate'))
    await click('[data-testid="save-site"]')
    await vi.waitFor(() => expect(create).toHaveBeenCalledOnce())
    await vi.waitFor(() => expect(document.querySelector<HTMLButtonElement>('[data-testid="save-site"]')?.disabled).toBe(false))
    expect(document.querySelector<HTMLInputElement>('[name="siteName"]')?.value).toBe(' New site ')
    create.mockResolvedValueOnce(row)
    await click('[data-testid="save-site"]')
    await vi.waitFor(() => expect(ElMessage.success).toHaveBeenCalledWith('站点已创建，当前为停用状态'))
    expect(create).toHaveBeenLastCalledWith({ name: 'New site', code: 'new-site', domains: [{ domain: 'new.example', primary: true }] })
    expect(page).toHaveBeenCalledTimes(2)
  })

  it('edits a cloned domain list, locks the code and can change the primary domain', async () => {
    mount()
    await vi.waitFor(() => expect(document.querySelector('[data-edit-site="7"]')).not.toBeNull())
    await click('[data-edit-site="7"]')
    await vi.waitFor(() => expect(document.querySelector('[name="siteCode"]')).not.toBeNull())
    expect(document.querySelector<HTMLInputElement>('[name="siteCode"]')!.disabled).toBe(true)
    const radios = document.querySelectorAll<HTMLInputElement>('[name="site-primary"]')
    radios[1].click()
    await nextTick()
    await input('siteName', 'Updated')
    update.mockResolvedValueOnce(row)
    await click('[data-testid="save-site"]')
    await vi.waitFor(() => expect(update).toHaveBeenCalledWith(7, { name: 'Updated', code: 'existing',
      domains: [{ domain: 'existing.example', primary: false }, { domain: 'alias.example', primary: true }] }))
    expect(row.domains[0].primary).toBe(true)
  })

  it('adds and removes domain rows while maintaining one primary', async () => {
    mount()
    await click('[data-testid="create-site"]')
    await vi.waitFor(() => expect(document.querySelector('[name="siteName"]')).not.toBeNull())
    await button('添加域名')
    await input('siteName', 'New')
    await input('siteCode', 'new')
    await input('siteDomain-0', 'old.example')
    await input('siteDomain-1', 'new.example')
    await click('[aria-label="删除域名 1"]')
    create.mockResolvedValueOnce(row)
    await click('[data-testid="save-site"]')
    await vi.waitFor(() => expect(create).toHaveBeenCalledWith({ name: 'New', code: 'new', domains: [{ domain: 'new.example', primary: true }] }))
  })

  it('requires confirmation before changing status and leaves the list intact on cancellation', async () => {
    const confirm = vi.spyOn(ElMessageBox, 'confirm').mockRejectedValueOnce('cancel')
    mount()
    await vi.waitFor(() => expect(document.querySelector('[data-status-site="7"]')).not.toBeNull())
    await click('[data-status-site="7"]')
    await vi.waitFor(() => expect(confirm).toHaveBeenCalledOnce())
    expect(status).not.toHaveBeenCalled()
    await vi.waitFor(() => expect(document.querySelector<HTMLButtonElement>('[data-status-site="7"]')?.disabled).toBe(false))
    confirm.mockImplementationOnce(() => Promise.resolve('confirm') as ReturnType<typeof ElMessageBox.confirm>)
    status.mockResolvedValueOnce({ ...row, status: 'ENABLED' })
    await click('[data-status-site="7"]')
    await vi.waitFor(() => expect(status).toHaveBeenCalledWith(7, 'ENABLED'))
    await vi.waitFor(() => expect(page).toHaveBeenCalledTimes(2))
  })

  it('shows a retry on load failure and submits search/reset filters', async () => {
    page.mockRejectedValueOnce(new Error('offline'))
    mount()
    await vi.waitFor(() => expect(document.body.textContent).toContain('站点加载失败'))
    await button('重新加载')
    await vi.waitFor(() => expect(document.querySelector('[data-edit-site="7"]')).not.toBeNull())
    await input('siteKeyword', ' existing.example ')
    await button('搜索')
    await vi.waitFor(() => expect(page).toHaveBeenLastCalledWith({ keyword: 'existing.example', status: undefined, page: 1, pageSize: 20 }))
    await vi.waitFor(() => expect([...document.querySelectorAll<HTMLButtonElement>('button')].find(b => b.textContent?.trim() === '重置')?.disabled).toBe(false))
    await button('重置')
    await vi.waitFor(() => expect(page).toHaveBeenLastCalledWith({ keyword: undefined, status: undefined, page: 1, pageSize: 20 }))
  })
})

