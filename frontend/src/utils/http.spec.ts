import { afterEach, describe, expect, it, vi } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

vi.mock('@/router', () => ({ default: { currentRoute: { value: { path: '/' } }, replace: vi.fn() } }))
vi.mock('@/stores', () => ({ pinia: {} }))
vi.mock('@/stores/user', () => ({ useUserStore: () => ({ token: 'test-token', clearAuth: vi.fn() }) }))
vi.mock('element-plus', () => ({ ElMessage: { error: vi.fn() } }))
afterEach(() => { vi.unstubAllEnvs(); vi.resetModules() })

describe('request base URL', () => {
  it('uses the production configuration without duplicating /api and keeps uploads on the same origin', async () => {
    const config = readFileSync(resolve(process.cwd(), '.env.production'), 'utf8')
    const baseURL = config.match(/^VITE_API_BASE_URL=(.*)$/m)?.[1].trim()
    expect(baseURL).toBe('')
    vi.stubEnv('VITE_API_BASE_URL', baseURL)
    const { http } = await import('./http')
    const { resolveAssetUrl } = await import('./asset')
    expect(http.getUri({ url: '/api/admin/site' })).toBe('/api/admin/site')
    expect(http.getUri({ url: '/api/public/home' })).toBe('/api/public/home')
    expect(resolveAssetUrl('/uploads/logo.png')).toBe('/uploads/logo.png')
  })
  it('keeps the development backend origin, token header and envelope unwrapping', async () => {
    vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080')
    const { http } = await import('./http')
    expect(http.getUri({ url: '/api/admin/site' })).toBe('http://localhost:8080/api/admin/site')
    const data = { siteTitle: '站点' }
    const result = await http.get('/api/admin/site', { adapter: async config => {
      expect(config.headers.Authorization).toBe('Bearer test-token')
      return { data: { code: 'OK', message: 'success', data }, status: 200, statusText: 'OK', headers: {}, config }
    } })
    expect(result).toEqual(data)
  })

  it('normalizes a legacy base URL that already ends with /api', async () => {
    vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080/api')
    const { http } = await import('./http')
    expect(http.getUri({ url: '/api/admin/site' })).toBe('http://localhost:8080/api/admin/site')
  })
})
