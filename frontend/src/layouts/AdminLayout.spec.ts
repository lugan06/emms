import { afterEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, nextTick, reactive, type App, type Slots } from 'vue'
import ElementPlus from 'element-plus'
import AdminLayout from './AdminLayout.vue'

const { state } = vi.hoisted(() => ({ state: { user: { roleCode: 'EDITOR', username: 'editor' }, siteName: 'Test' } }))
vi.mock('@/stores/user', () => ({ useUserStore: () => state }))
vi.mock('@/components/admin/PasswordChangeDialog.vue', () => ({ default: { render: () => null } }))
vi.mock('vue-router', async () => {
  const { h } = await import('vue')
  return {
    useRouter: () => ({ replace: vi.fn() }),
    useRoute: () => ({ path: '/admin/sites', meta: { title: '站点管理' } }),
    RouterView: { render: () => null },
    RouterLink: { props: ['to'], setup: (props: { to: string }, { slots }: { slots: Slots }) =>
      () => h('a', { href: props.to }, slots.default?.()) },
  }
})
let app: App | undefined
let container: HTMLDivElement
afterEach(() => { app?.unmount(); container?.remove() })

describe('site management navigation', () => {
  it('only displays the sites menu to SUPER_ADMIN and highlights it independently of site config', async () => {
    const user = reactive(state.user)
    state.user = user
    container = document.createElement('div')
    document.body.append(container)
    app = createApp({ render: () => h(AdminLayout) })
    app.use(ElementPlus).mount(container)
    expect(container.querySelector('a[href="/admin/sites"]')).toBeNull()
    expect(container.querySelector('a[href="/admin/users"]')).toBeNull()
    user.roleCode = 'SUPER_ADMIN'
    await nextTick()
    expect(container.querySelector('a[href="/admin/sites"]')?.textContent).toContain('站点管理')
    expect(container.querySelector('a[href="/admin/users"]')?.textContent).toContain('管理员管理')
    expect(container.querySelector('a[href="/admin/sites"]')?.classList.contains('menu-active')).toBe(true)
    expect(container.querySelector('a[href="/admin/site"]')?.classList.contains('menu-active')).toBe(false)
    user.roleCode = 'EDITOR'
    await nextTick()
    expect(container.querySelector('a[href="/admin/sites"]')).toBeNull()
    expect(container.querySelector('a[href="/admin/users"]')).toBeNull()
  })
})
