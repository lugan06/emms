import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, nextTick, type App } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus, { ElMessage } from 'element-plus'
import SiteConfigView from './SiteConfigView.vue'
import { useUserStore } from '@/stores/user'
import { usePublicStore } from '@/stores/public'
import { createSiteForm } from '@/utils/system'
import { createFooterInfo, parseFooterInfo, serializeFooterInfo } from '@/utils/footer'

const { site, updateSite, options } = vi.hoisted(() => ({ site: vi.fn(), updateSite: vi.fn(), options: vi.fn() }))
vi.mock('@/api/system', () => ({ systemApi: { site, updateSite } }))
vi.mock('@/utils/admin-options', () => ({ loadExhibitionOptions: options }))
vi.mock('@/components/admin/CoverImageUpload.vue', () => ({ default: { template: '<div />' } }))
vi.mock('@/components/admin/HeroVideoUpload.vue', () => ({ default: { template: '<div />' } }))

let app: App | undefined
let container: HTMLDivElement
const legacy = '主办单位：主办甲 | 承办单位：承办乙\n定展热线：13700000000 | 联系人：张先生 | 邮箱：old@example.test\n其他说明'

async function mount(roleCode = 'SUPER_ADMIN') {
  const pinia = createPinia()
  const user = useUserStore(pinia)
  user.user = { id: 1, username: 'admin', nickname: null, avatarUrl: null, roleCode }
  container = document.createElement('div')
  document.body.append(container)
  app = createApp({ render: () => h(SiteConfigView) })
  app.use(pinia).use(ElementPlus).mount(container)
  await vi.waitFor(() => expect(container.querySelector('[name="footerOrganizer"]')).not.toBeNull())
  return usePublicStore(pinia)
}

async function input(name: string, value: string) {
  const element = container.querySelector<HTMLInputElement>(`[name="${name}"]`)!
  element.value = value
  element.dispatchEvent(new Event('input', { bubbles: true }))
  await nextTick()
}

async function submit() {
  container.querySelector('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }))
  await nextTick()
}

beforeEach(() => {
  vi.resetAllMocks()
  site.mockResolvedValue({ ...createSiteForm(), id: 1, configCode: 'default', siteTitle: '站点名称', footerInfo: legacy })
  options.mockResolvedValue([])
  vi.spyOn(ElMessage, 'success').mockImplementation(() => ({ close: () => {} }))
})
afterEach(() => { app?.unmount(); app = undefined; container?.remove(); vi.restoreAllMocks() })

describe('editable footer settings', () => {
  it('loads legacy footer fields, saves independent values and preserves them after reloading', async () => {
    const store = await mount()
    store.site = createSiteForm()
    expect(container.querySelector<HTMLInputElement>('[name="footerOrganizer"]')?.value).toBe('主办甲')
    expect(container.querySelector<HTMLInputElement>('[name="footerContractor"]')?.value).toBe('承办乙')
    expect(container.querySelector<HTMLInputElement>('[name="footerPhone"]')?.value).toBe('13700000000')
    expect(container.querySelector<HTMLTextAreaElement>('[name="footerExtraText"]')?.value).toBe('其他说明')
    await input('footerOrganizer', ' 新主办单位 ')
    await input('footerEmail', 'new@example.test')
    await input('footerCopyrightOwner', '版权单位')
    updateSite.mockResolvedValueOnce({})
    await submit()
    await vi.waitFor(() => expect(ElMessage.success).toHaveBeenCalledWith('站点配置保存成功'))
    const payload = updateSite.mock.calls[0][0]
    expect(parseFooterInfo(payload.footerInfo)).toEqual({ organizer: '新主办单位', contractor: '承办乙',
      phone: '13700000000', contactName: '张先生', email: 'new@example.test', copyrightOwner: '版权单位', extraText: '其他说明' })
    expect(payload.siteTitle).toBe('站点名称')
    expect(store.site).toBeNull()
    site.mockResolvedValueOnce({ ...payload, id: 1, configCode: 'default' })
    const reload = [...container.querySelectorAll<HTMLButtonElement>('button')].find(button => button.textContent === '重新加载')!
    reload.click()
    await vi.waitFor(() => expect(site).toHaveBeenCalledTimes(2))
    await vi.waitFor(() => expect(container.querySelector<HTMLInputElement>('[name="footerOrganizer"]')?.value).toBe('新主办单位'))
  })

  it('blocks invalid email, retains failed edits and allows clearing footer fields', async () => {
    await mount()
    await input('footerEmail', 'invalid')
    await submit()
    await vi.waitFor(() => expect(container.textContent).toContain('请输入有效的页脚邮箱地址'))
    expect(updateSite).not.toHaveBeenCalled()
    await input('footerEmail', 'valid@example.test')
    updateSite.mockRejectedValueOnce(new Error('save failed'))
    await submit()
    await vi.waitFor(() => expect(updateSite).toHaveBeenCalledOnce())
    await vi.waitFor(() => expect(container.querySelector<HTMLButtonElement>('[type="submit"]')?.disabled).toBe(false))
    expect(container.querySelector<HTMLInputElement>('[name="footerEmail"]')?.value).toBe('valid@example.test')
    for (const name of ['Organizer', 'Contractor', 'Phone', 'ContactName', 'Email', 'CopyrightOwner', 'ExtraText']) await input(`footer${name}`, '')
    updateSite.mockResolvedValueOnce({})
    await submit()
    await vi.waitFor(() => expect(updateSite).toHaveBeenCalledTimes(2))
    expect(updateSite.mock.calls[1][0].footerInfo).toBe('')
  })

  it('loads structured settings and keeps editors read-only', async () => {
    site.mockResolvedValueOnce({ ...createSiteForm(), siteTitle: '站点',
      footerInfo: serializeFooterInfo({ ...createFooterInfo(), organizer: '表单配置的单位' }) })
    await mount('EDITOR')
    const organizer = container.querySelector<HTMLInputElement>('[name="footerOrganizer"]')!
    expect(organizer.value).toBe('表单配置的单位')
    expect(organizer.disabled).toBe(true)
    await submit()
    expect(updateSite).not.toHaveBeenCalled()
  })
})
