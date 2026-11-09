import { afterEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, type App } from 'vue'
import { createPinia } from 'pinia'
import HomeView from './HomeView.vue'

const { home } = vi.hoisted(() => ({ home: vi.fn() }))
vi.mock('@/api/public', () => ({ publicApi: { home } }))
vi.mock('@/components/public/HomeHero.vue', () => ({ default: { template: '<div />' } }))
vi.mock('@/components/public/CurrentExhibitionSection.vue', () => ({ default: { template: '<div />' } }))
vi.mock('@/components/public/PublicNewsMarquee.vue', () => ({ default: { template: '<div />' } }))
vi.mock('@/components/public/PublicContentSection.vue', () => ({ default: {
  props: ['title', 'items'], template: '<section><h2>{{ title }}</h2><p v-for="item in items" :key="item.id">{{ item.title }} {{ item.body }}</p></section>',
} }))
let app: App | undefined
let container: HTMLDivElement
afterEach(() => { app?.unmount(); container?.remove(); vi.resetAllMocks() })

describe('dynamic home overview', () => {
  it.each([false, true])('uses the complete ordered overview without repeating legacy sections (empty=%s)', async empty => {
    const items = Array.from({ length: 8 }, (_, id) => ({ id, title: `定价 ${id}`, body: `完整正文 ${id}` }))
    home.mockResolvedValue({ site: {}, company: {}, currentExhibition: null,
      exhibitionIntro: [{ id: 99, title: '旧简介' }], exhibitionScope: [{ id: 100, title: '旧范围' }],
      samePeriodActivities: [], exhibitionNews: [], industryNews: [], exhibitorNews: [],
      overviewSections: empty ? [] : [
        { categoryId: 20, categoryCode: 'exhibition-price', categoryName: '展位定价', items },
        { categoryId: 21, categoryCode: 'custom', categoryName: '自定义子栏目', items: [{ id: 88, title: '新增内容', body: '完整正文' }] },
      ],
    })
    container = document.createElement('div')
    document.body.append(container)
    app = createApp({ render: () => h(HomeView) })
    for (const name of ['el-skeleton', 'el-button', 'el-result']) {
      app.component(name, { template: '<div><slot /></div>' })
    }
    app.use(createPinia()).mount(container)
    await vi.waitFor(() => expect(home).toHaveBeenCalledOnce())
    await vi.waitFor(() => expect(container.querySelectorAll('section h2')).toHaveLength(empty ? 0 : 2))
    expect(container.textContent).not.toContain('旧简介')
    expect(container.textContent).not.toContain('旧范围')
    if (!empty) {
      expect([...container.querySelectorAll('h2')].map(el => el.textContent)).toEqual(['展位定价', '自定义子栏目'])
      expect(container.textContent).toContain('完整正文 7')
      expect(container.textContent).toContain('新增内容')
    }
  })
})
