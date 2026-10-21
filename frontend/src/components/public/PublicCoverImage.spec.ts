import { afterEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, nextTick, reactive, type App } from 'vue'
import ElementPlus from 'element-plus'
import PublicCoverImage from './PublicCoverImage.vue'

let app: App | undefined
let container: HTMLDivElement

function mount(src: string | null) {
  const props = reactive({ src, alt: '展会封面' })
  container = document.createElement('div')
  document.body.append(container)
  app = createApp({ render: () => h(PublicCoverImage, props) })
  app.use(ElementPlus).mount(container)
  return props
}

afterEach(() => {
  app?.unmount()
  app = undefined
  container?.remove()
  vi.unstubAllEnvs()
})

describe('public cover image', () => {
  it('shows an accessible placeholder without requesting an empty URL', () => {
    mount(null)
    expect(container.querySelector('img')).toBeNull()
    expect(container.querySelector('[role="img"]')?.getAttribute('aria-label')).toBe('展会封面')
    expect(container.textContent).toContain('EEMS')
  })

  it('resolves relative upload paths and recovers from an error when the URL changes', async () => {
    vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080/api')
    const props = mount('/uploads/cover.jpg')
    await nextTick()
    const image = container.querySelector('img')!
    expect(image.getAttribute('src')).toBe('http://localhost:8080/uploads/cover.jpg')
    expect(image.getAttribute('alt')).toBe('展会封面')
    image.dispatchEvent(new Event('error'))
    await vi.waitFor(() => expect(container.querySelector('.cover-placeholder')).not.toBeNull())

    props.src = 'https://images.example.test/new-cover.jpg'
    await vi.waitFor(() => expect(container.querySelector('img')?.getAttribute('src')).toBe(props.src))
    expect(container.querySelector('.cover-placeholder')).toBeNull()
  })
})
