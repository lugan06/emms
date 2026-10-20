import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const { company } = vi.hoisted(() => ({ company: vi.fn() }))
vi.mock('@/api/public', () => ({ publicApi: { company } }))

describe('usePublicStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.resetAllMocks()
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
