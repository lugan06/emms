import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const { messageError } = vi.hoisted(() => ({ messageError: vi.fn() }))
vi.mock('@/router', () => ({ default: { currentRoute: { value: { path: '/' } }, replace: vi.fn() } }))
vi.mock('@/stores', () => ({ pinia: {} }))
vi.mock('@/stores/user', () => ({ useUserStore: () => ({ token: 'test-token', clearAuth: vi.fn() }) }))
vi.mock('element-plus', () => ({ ElMessage: { error: messageError } }))
beforeEach(() => { messageError.mockClear() })
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
  it('uses same-origin development requests and assets while preserving authentication and response unwrapping', async () => {
    vi.stubEnv('VITE_API_BASE_URL', '')
    const { http } = await import('./http')
    const { resolveAssetUrl } = await import('./asset')
    expect(http.getUri({ url: '/api/admin/site' })).toBe('/api/admin/site')
    expect(http.getUri({ url: '/api/public/home' })).toBe('/api/public/home')
    expect(resolveAssetUrl('/uploads/logo.png')).toBe('/uploads/logo.png')
    const data = { siteTitle: '站点' }
    const result = await http.get('/api/admin/site', { adapter: async config => {
      expect(config.headers.Authorization).toBe('Bearer test-token')
      return { data: { code: 'OK', message: 'success', data }, status: 200, statusText: 'OK', headers: {}, config }
    } })
    expect(result).toEqual(data)
  })

  it('keeps FormData intact instead of serializing it as JSON', async () => {
    vi.stubEnv('VITE_API_BASE_URL', '')
    const { http } = await import('./http')
    const formData = new FormData()
    formData.append('file', new File(['video'], 'clip.mp4', { type: 'video/mp4' }))

    await http.post('/api/admin/files', formData, { adapter: async config => {
      expect(config.data).toBeInstanceOf(FormData)
      expect(config.data.get('file')).toBeInstanceOf(File)
      expect(config.headers.getContentType()).not.toBe('application/json')
      return { data: { code: 'OK', message: 'success', data: {} }, status: 200, statusText: 'OK', headers: {}, config }
    } })
  })

  it('sets JSON content type automatically for object payloads', async () => {
    vi.stubEnv('VITE_API_BASE_URL', '')
    const { http } = await import('./http')
    const payload = { siteTitle: '展会官网' }

    await http.put('/api/admin/site', payload, { adapter: async config => {
      expect(config.data).toBe(JSON.stringify(payload))
      expect(config.headers.getContentType()).toContain('application/json')
      return { data: { code: 'OK', message: 'success', data: {} }, status: 200, statusText: 'OK', headers: {}, config }
    } })
  })

  it('normalizes a legacy base URL that already ends with /api', async () => {
    vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080/api')
    const { http } = await import('./http')
    expect(http.getUri({ url: '/api/admin/site' })).toBe('http://localhost:8080/api/admin/site')
  })

  it('maps site business errors returned in a successful HTTP response', async () => {
    vi.stubEnv('VITE_API_BASE_URL', '')
    const { http } = await import('./http')

    await expect(http.get('/api/admin/site', { adapter: async config => ({
      data: { code: 'SITE_ADMIN_FORBIDDEN', message: 'backend detail', data: null },
      status: 200, statusText: 'OK', headers: {}, config,
    }) })).rejects.toThrow('当前域名无管理权限')
    expect(messageError).toHaveBeenCalledWith('当前域名无管理权限')
  })

  it('maps site business errors returned with an HTTP error response', async () => {
    vi.stubEnv('VITE_API_BASE_URL', '')
    const { http } = await import('./http')

    await expect(http.get('/api/admin/site', { adapter: async config => {
      throw { response: { status: 403, data: { code: 'SITE_DISABLED', message: 'backend detail' } }, config }
    } })).rejects.toBeDefined()
    expect(messageError).toHaveBeenCalledWith('站点已停用')
  })
})
