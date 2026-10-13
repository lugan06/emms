import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { NavigationGuard } from 'vue-router'

const { state, warning, beforeEachGuard } = vi.hoisted(() => ({
  state: { isLoggedIn: false, userVerified: false, user: null as { roleCode: string } | null, refreshUser: vi.fn(), clearAuth: vi.fn() },
  warning: vi.fn(), beforeEachGuard: vi.fn(),
}))
vi.mock('@/stores', () => ({ pinia: {} }))
vi.mock('@/stores/user', () => ({ useUserStore: () => state }))
vi.mock('element-plus', () => ({ ElMessage: { warning } }))
vi.mock('vue-router', async importOriginal => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return { ...actual, createRouter: () => ({ beforeEach: beforeEachGuard }) }
})
await import('./index')
const guard = beforeEachGuard.mock.calls[0][0] as NavigationGuard
async function access(meta: Record<string, boolean>, fullPath: string) {
  return (guard as (to: unknown) => unknown)({ meta, fullPath })
}
beforeEach(() => { state.isLoggedIn = false; state.userVerified = false; state.user = null; state.refreshUser.mockReset(); state.clearAuth.mockClear(); warning.mockClear() })
describe('admin route access', () => {
  it('preserves the protected destination when sending an anonymous visitor to login', async () => {
    expect(await access({ requiresAuth: true }, '/admin/logs')).toEqual({ name: 'admin-login', query: { redirect: '/admin/logs' } })
  })
  it('refreshes a restored user before checking privileged access', async () => {
    state.isLoggedIn = true
    state.refreshUser.mockImplementation(async () => { state.user = { roleCode: 'EDITOR' }; state.userVerified = true })
    expect(await access({ requiresAuth: true, requiresSuperAdmin: true }, '/admin/logs')).toEqual({ name: 'admin-dashboard' })
    expect(state.refreshUser).toHaveBeenCalledOnce(); expect(warning).toHaveBeenCalled()
  })
  it('permits verified super administrators to access logs and editors to read configuration', async () => {
    state.isLoggedIn = true; state.userVerified = true; state.user = { roleCode: 'SUPER_ADMIN' }
    expect(await access({ requiresAuth: true, requiresSuperAdmin: true }, '/admin/logs')).toBe(true)
    state.user = { roleCode: 'EDITOR' }
    expect(await access({ requiresAuth: true }, '/admin/site')).toBe(true)
  })
  it('does not continue into a protected page after current-user verification fails', async () => {
    state.isLoggedIn = true; state.refreshUser.mockRejectedValue(new Error('network failure'))
    expect(await access({ requiresAuth: true }, '/admin/site')).toEqual({ name: 'admin-login', query: { redirect: '/admin/site' } })
    expect(state.clearAuth).toHaveBeenCalledOnce()
  })
})
