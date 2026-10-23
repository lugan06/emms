import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, type App, type Component } from 'vue'
import { createPinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import ElementPlus from 'element-plus'
import HomeView from './HomeView.vue'
import ContactView from './ContactView.vue'
import ContentView from './ContentView.vue'
import CategoryContentView from './CategoryContentView.vue'
import ExhibitionListView from './ExhibitionListView.vue'
import { usePublicStore } from '@/stores/public'
import { userRoutes } from '@/router/user'
import type { PublicCompany, PublicContent, PublicHome } from '@/types/public'

const { home, company, contentDetail, categoryContents, exhibitions } = vi.hoisted(() => ({
  home: vi.fn(), company: vi.fn(), contentDetail: vi.fn(), categoryContents: vi.fn(), exhibitions: vi.fn(),
}))
vi.mock('@/api/public', () => ({ publicApi: { home, company, contentDetail, categoryContents, exhibitions } }))
vi.mock('@/components/public/PublicGuestbookForm.vue', () => ({
  default: { template: '<section id="online-guestbook">在线留言</section>' },
}))

const contactCompany: PublicCompany = {
  companyName: '配置的联系公司', contactName: '联系人甲', mobile: '13800138000', telephone: '021-12345678',
  email: 'contact@example.test', address: '配置的联系地址', postalCode: '200000', wechatImageUrl: null,
}

function homeFixture(): PublicHome {
  return {
    site: { siteTitle: '测试展会', siteSubtitle: null, domain: null, logoUrl: null, keywords: null, description: null, icpNumber: null, footerInfo: null, heroVideoUrl: null },
    company: contactCompany, currentExhibition: null,
    exhibitionIntro: [], samePeriodActivities: [], exhibitionScope: [], exhibitionNews: [], industryNews: [], exhibitorNews: [],
  }
}

function item(id: number, title: string): PublicContent {
  return {
    id, title, categoryId: 2, categoryCode: 'exhibition-intro', categoryName: '展会简介', exhibitionId: 1,
    slug: null, coverUrl: null, summary: '公开内容摘要', author: null, source: null,
    body: '<p>完整的公开正文</p>',
    publishedAt: '2026-01-01T10:00:00', isTop: 0, isRecommend: 0,
  }
}

let app: App | undefined
let container: HTMLDivElement

async function mount(component: Component, path = '/') {
  const pinia = createPinia()
  const router = createRouter({ history: createMemoryHistory(), routes: [userRoutes] })
  await router.push(path)
  await router.isReady()
  container = document.createElement('div')
  document.body.append(container)
  app = createApp({ render: () => h(component) })
  app.use(pinia).use(router).use(ElementPlus).mount(container)
  return { store: usePublicStore(pinia), router }
}

beforeEach(() => { vi.resetAllMocks() })
afterEach(() => { app?.unmount(); app = undefined; container?.remove() })

describe('home content sections', () => {
  it.each(['video', 'source'])('renders the video hero and retains its cover after a %s error', async errorTarget => {
    const value = homeFixture()
    value.site.heroVideoUrl = '/uploads/sites/1/hero-video/test.mp4'
    value.currentExhibition = {
      id: 3, exhibitionCode: 'EEMS-2027', title: '绿色涂料展', subtitle: null,
      year: 2027, edition: null, coverUrl: '/uploads/exhibitions/hero-cover.jpg',
      summary: '展会摘要', description: null, venue: '上海新国际博览中心', address: null,
      startAt: '2027-03-10T09:00:00', endAt: '2027-03-12T17:00:00',
      publishedAt: null, contactName: null, contactPhone: null, registrationUrl: null,
      showOnHome: 1, homeSort: 1, isCurrent: true,
    }
    home.mockResolvedValue(value)
    await mount(HomeView)

    await vi.waitFor(() => expect(container.querySelector('.hero-video')).not.toBeNull())
    const video = container.querySelector<HTMLVideoElement>('.hero-video')!
    expect(video.hasAttribute('autoplay')).toBe(true)
    expect(video.muted || video.defaultMuted).toBe(true)
    expect(video.hasAttribute('loop')).toBe(true)
    expect(video.hasAttribute('playsinline')).toBe(true)
    expect(video.querySelector('source')?.getAttribute('src')).toBe('/uploads/sites/1/hero-video/test.mp4')
    expect(container.querySelector<HTMLElement>('.hero-poster')?.style.backgroundImage).toContain('hero-cover.jpg')
    video.dispatchEvent(new Event('canplay'))
    await vi.waitFor(() => expect(video.classList.contains('is-ready')).toBe(true))

    const target = errorTarget === 'source' ? video.querySelector('source')! : video
    target.dispatchEvent(new Event('error'))
    await vi.waitFor(() => expect(container.querySelector('.hero-video')).toBeNull())
    expect(container.querySelector<HTMLElement>('.hero-poster')?.style.backgroundImage).toContain('hero-cover.jpg')
  })

  it('does not render a hero video when the site has no configured video', async () => {
    home.mockResolvedValue(homeFixture())
    await mount(HomeView)

    await vi.waitFor(() => expect(container.textContent).toContain('测试展会'))
    expect(container.querySelector('.hero-video')).toBeNull()
    expect(container.querySelector('.hero-poster')).not.toBeNull()
  })

  it('shows the current exhibition schedule, venue and detail link without repeating its title', async () => {
    const value = homeFixture()
    value.currentExhibition = {
      id: 3, exhibitionCode: 'EEMS-2027', title: '首页右侧标题测试', subtitle: null,
      year: 2027, edition: null, coverUrl: null, summary: '展会摘要', description: null,
      venue: '上海国家会展中心（虹桥）', address: null,
      startAt: '2027-06-16T09:00:00', endAt: '2027-06-18T14:00:00',
      publishedAt: null, contactName: null, contactPhone: null, registrationUrl: null,
      showOnHome: 1, homeSort: 1, isCurrent: true,
    }
    home.mockResolvedValue(value)
    await mount(HomeView)

    await vi.waitFor(() => expect(container.querySelector('.hero-detail-link')).not.toBeNull())
    const exhibitionCopy = container.querySelector('.hero-exhibition-copy')!
    expect(exhibitionCopy.querySelector('h2')).toBeNull()
    expect(exhibitionCopy.textContent).not.toContain('首页右侧标题测试')
    expect(exhibitionCopy.textContent).toContain('2027/06/16 09:00 - 2027/06/18 14:00')
    expect(exhibitionCopy.textContent).toContain('上海国家会展中心（虹桥）')
    expect(exhibitionCopy.querySelector('.hero-exhibition-date')?.textContent).toContain('2027/06/16')
    expect(exhibitionCopy.querySelector('.hero-exhibition-venue')?.textContent).toBe('上海国家会展中心（虹桥）')
    expect(exhibitionCopy.querySelector('.hero-detail-link')?.getAttribute('href')).toBe('/exhibitions/3')
  })

  it('renders returned introduction and scope and stores the site information', async () => {
    const value = homeFixture()
    value.exhibitionIntro = [item(102, '已发布的展会简介')]
    value.exhibitionScope = [item(104, '已发布的参展范围')]
    home.mockResolvedValue(value)
    const { store } = await mount(HomeView)

    await vi.waitFor(() => expect(container.textContent).toContain('已发布的展会简介'))
    expect(container.textContent).toContain('已发布的参展范围')
    expect(container.querySelectorAll('.content-body')).toHaveLength(2)
    expect(container.querySelector('.content-body p')?.textContent).toBe('完整的公开正文')
    expect(container.querySelectorAll('.content-section')).toHaveLength(2)
    expect(container.textContent).not.toContain('暂无内容')
    expect(container.textContent).not.toContain('展商新闻')
    expect(store.site?.siteTitle).toBe('测试展会')
    expect(store.company).toEqual(contactCompany)
  })

  it.each([
    ['exhibitionIntro', '展会简介'], ['samePeriodActivities', '同期活动'], ['exhibitionScope', '参展范围'],
  ] as const)('automatically displays published content in %s', async (field, title) => {
    const value = homeFixture()
    value[field] = [item(101, '新增公开内容')]
    home.mockResolvedValue(value)
    await mount(HomeView)

    await vi.waitFor(() => expect(container.querySelector('.content-section h2')?.textContent).toBe(title))
    expect(container.textContent).toContain('新增公开内容')
    expect(container.querySelector('.content-body p')?.textContent).toBe('完整的公开正文')
    expect(container.querySelectorAll('.content-section')).toHaveLength(1)
  })

  it.each([
    ['exhibitionNews', '展会动态'], ['industryNews', '行业新闻'], ['exhibitorNews', '展商新闻'],
  ] as const)('shows %s inside the homepage news marquee', async (field, categoryName) => {
    const value = homeFixture()
    const newsItem = { ...item(201, `来自${categoryName}的新闻`), categoryName }
    value[field] = [newsItem]
    home.mockResolvedValue(value)
    await mount(HomeView)

    await vi.waitFor(() => expect(container.querySelector('.news-marquee')).not.toBeNull())
    expect(container.querySelector('.news-marquee')?.textContent).toContain(newsItem.title)
    expect(container.querySelector('.news-marquee .news-category')?.textContent).toBe(categoryName)
    expect(container.querySelector('.news-card')?.getAttribute('href')).toBe('/content/201')
    expect(container.querySelector('.content-section')).toBeNull()
  })

  it('combines news columns into one seamless marquee and duplicates the sequence for looping', async () => {
    const value = homeFixture()
    value.exhibitionNews = [{ ...item(201, '展会动态新闻'), categoryName: '展会动态', coverUrl: '/uploads/news/exhibition.jpg' }]
    value.industryNews = [{ ...item(202, '行业新闻'), categoryName: '行业新闻' }]
    value.exhibitorNews = [{ ...item(203, '展商新闻'), categoryName: '展商新闻' }]
    home.mockResolvedValue(value)
    await mount(HomeView)

    await vi.waitFor(() => expect(container.querySelectorAll('.news-marquee .news-card')).toHaveLength(8))
    expect(container.querySelectorAll('.news-marquee .news-group')).toHaveLength(2)
    expect(container.querySelectorAll('.news-marquee .news-group')[1].getAttribute('aria-hidden')).toBe('true')
    for (const id of [201, 202, 203]) {
      expect(container.querySelector(`.news-card[href="/content/${id}"]`)).not.toBeNull()
    }
    expect(container.querySelector('.news-card .news-cover img')?.getAttribute('src')).toBe('/uploads/news/exhibition.jpg')
  })

  it('keeps the home page available without displaying empty or missing modules', async () => {
    home.mockResolvedValue(homeFixture())
    await mount(HomeView)
    await vi.waitFor(() => expect(container.textContent).toContain('测试展会'))
    expect(container.querySelectorAll('.content-section')).toHaveLength(0)
    expect(container.querySelector('.news-marquee')).toBeNull()
    expect(container.textContent).not.toContain('暂无内容')
  })

  it('renders complete rich text including paragraphs, images, lists and tables', async () => {
    const value = homeFixture()
    const content = item(102, '展会简介')
    content.body = '<h2>展会亮点</h2><p>第一段正文</p><p>末段完整正文</p>'
      + '<img src="/uploads/intro.png" alt="展会图片">'
      + '<ul><li>人形机器人</li><li>核心零部件</li></ul>'
      + '<table><tbody><tr><td>展馆信息</td></tr></tbody></table>'
    value.exhibitionIntro = [content]
    home.mockResolvedValue(value)
    await mount(HomeView)

    await vi.waitFor(() => expect(container.querySelector('.content-body h2')?.textContent).toBe('展会亮点'))
    expect(container.querySelectorAll('.content-body p')).toHaveLength(2)
    expect(container.querySelector('.content-body')?.textContent).toContain('末段完整正文')
    expect(container.querySelector('.content-body img')?.getAttribute('src')).toBe('/uploads/intro.png')
    expect(container.querySelectorAll('.content-body li')).toHaveLength(2)
    expect(container.querySelector('.content-body td')?.textContent).toBe('展馆信息')
    expect(contentDetail).not.toHaveBeenCalled()
  })

  it('retains the title and summary when the published content has no body', async () => {
    const value = homeFixture()
    value.exhibitionScope = [{ ...item(104, '参展范围'), body: null }]
    home.mockResolvedValue(value)
    await mount(HomeView)

    await vi.waitFor(() => expect(container.querySelector('.content-item h3')?.textContent).toBe('参展范围'))
    expect(container.textContent).toContain('公开内容摘要')
    expect(container.querySelector('.content-body')).toBeNull()
  })

  it('reloads the home page after an API failure', async () => {
    home.mockRejectedValueOnce(new Error('首页暂不可用')).mockResolvedValueOnce(homeFixture())
    await mount(HomeView)
    await vi.waitFor(() => expect(container.textContent).toContain('首页暂时无法加载'))
    container.querySelector<HTMLButtonElement>('button')!.click()
    await vi.waitFor(() => expect(container.textContent).toContain('测试展会'))
    expect(home).toHaveBeenCalledTimes(2)
  })
})

describe('shared public page presentation', () => {
  it('renders article rich text and returns to its category', async () => {
    contentDetail.mockResolvedValue({
      ...item(102, '详情页展会简介'), coverUrl: '/uploads/intro.jpg',
      body: '<h2>亮点</h2><p>完整正文</p><ul><li>机器人</li></ul><table><tbody><tr><td>展馆</td></tr></tbody></table>',
    })
    const { router } = await mount(ContentView, '/content/102')
    await vi.waitFor(() => expect(container.querySelector('.article-body td')?.textContent).toBe('展馆'))
    expect(container.querySelector('.article-body li')?.textContent).toBe('机器人')
    expect(container.querySelector('.article-cover img')?.getAttribute('src')).toBe('/uploads/intro.jpg')
    container.querySelector<HTMLButtonElement>('.back-row button')!.click()
    await vi.waitFor(() => expect(router.currentRoute.value.path).toBe('/category/exhibition-intro'))
  })

  it('keeps category headings, empty-cover fallback and article navigation available', async () => {
    categoryContents.mockResolvedValue({ records: [item(102, '栏目文章')], total: 1, page: 1, pageSize: 10 })
    const { router } = await mount(CategoryContentView, '/category/exhibition-intro')
    await vi.waitFor(() => expect(container.querySelector('.page-heading h1')?.textContent).toBe('展会简介'))
    expect(container.querySelector('.content-cover .cover-placeholder')?.textContent).toBe('EEMS')
    container.querySelector<HTMLElement>('.content-card')!.click()
    await vi.waitFor(() => expect(router.currentRoute.value.path).toBe('/content/102'))
  })

  it('keeps the exhibition list heading and empty state available', async () => {
    exhibitions.mockResolvedValue({ records: [], total: 0, page: 1, pageSize: 12 })
    await mount(ExhibitionListView, '/exhibitions')
    await vi.waitFor(() => expect(container.textContent).toContain('暂无符合条件的展会'))
    expect(container.querySelector('.page-heading h1')?.textContent).toBe('展会列表')
  })
})

describe('contact page', () => {
  it('supports direct navigation and reads configured company details without a fixed content ID', async () => {
    company.mockResolvedValue(contactCompany)
    const { router } = await mount(ContactView, '/contact')
    expect(router.currentRoute.value.name).toBe('public-contact')
    await vi.waitFor(() => expect(container.textContent).toContain(contactCompany.companyName))
    for (const value of ['联系人甲', '13800138000', '021-12345678', 'contact@example.test', '配置的联系地址', '200000']) {
      expect(container.textContent).toContain(value)
    }
    expect(container.textContent).toContain('在线留言')
    expect(company).toHaveBeenCalledOnce()
    expect(contentDetail).not.toHaveBeenCalled()
    expect(home).not.toHaveBeenCalled()
  })

  it('keeps the guestbook available and retries contact information after a failure', async () => {
    company.mockRejectedValueOnce(new Error('联系信息暂不可用')).mockResolvedValueOnce(contactCompany)
    await mount(ContactView, '/contact')
    await vi.waitFor(() => expect(container.textContent).toContain('联系信息加载失败'))
    expect(container.textContent).toContain('在线留言')
    container.querySelector<HTMLButtonElement>('button')!.click()
    await vi.waitFor(() => expect(container.textContent).toContain(contactCompany.companyName))
    expect(container.textContent).not.toContain('联系信息加载失败')
    expect(company).toHaveBeenCalledTimes(2)
  })
})
