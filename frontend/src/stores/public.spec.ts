import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const { company, site } = vi.hoisted(() => ({ company: vi.fn(), site: vi.fn() }))
vi.mock('@/api/public', () => ({ publicApi: { company, site } }))

describe('usePublicStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.resetAllMocks()
  })

  it('shares in-flight site loads and caches the result for direct public page visits', async () => {
    const siteValue = { siteTitle: '测试站点', footerInfo: '主办单位：自定义主办方' }
    site.mockResolvedValueOnce(siteValue)
    const { usePublicStore } = await import('./public')
    const store = usePublicStore()
    await Promise.all([store.loadSite(), store.loadSite()])
    await store.loadSite()
    expect(site).toHaveBeenCalledOnce()
    expect(store.site).toEqual(siteValue)
  })

  it('allows site retries after failure and reloads after cache invalidation', async () => {
    site.mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce({ siteTitle: '恢复的站点' })
    const { usePublicStore } = await import('./public')
    const store = usePublicStore()
    await store.loadSite()
    expect(store.site).toBeNull()
    await store.loadSite()
    expect(store.site?.siteTitle).toBe('恢复的站点')
    store.site = null
    site.mockResolvedValueOnce({ siteTitle: '更新的站点' })
    await store.loadSite()
    expect(store.site).toMatchObject({ siteTitle: '更新的站点' })
  })

  it('loads company contact information once and stores the result', async () => {
    const companyValue = { companyName: '测试公司', telephone: '021-12345678' }
    company.mockResolvedValueOnce(companyValue)

    const { usePublicStore } = await import('./public')
    const store = usePublicStore()
    await store.loadCompany()
    await store.loadCompany()

    expect(store.company).toEqual(companyValue)
    expect(store.companyLoading).toBe(false)
    expect(company).toHaveBeenCalledOnce()
  })

  it('keeps retry available when loading company information fails', async () => {
    const companyValue = { companyName: '恢复后的公司信息' }
    company.mockRejectedValueOnce(new Error('暂时不可用')).mockResolvedValueOnce(companyValue)

    const { usePublicStore } = await import('./public')
    const store = usePublicStore()
    await store.loadCompany()
    expect(store.company).toBeNull()
    expect(store.companyLoading).toBe(false)

    await store.loadCompany()
    expect(store.company).toEqual(companyValue)
    expect(company).toHaveBeenCalledTimes(2)
  })
})
