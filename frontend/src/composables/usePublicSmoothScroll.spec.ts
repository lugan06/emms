import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, type App } from 'vue'
import { usePublicSmoothScroll } from './usePublicSmoothScroll'

const { createLenis, destroy } = vi.hoisted(() => ({ createLenis: vi.fn(), destroy: vi.fn() }))
vi.mock('lenis', () => ({ default: createLenis }))

let app: App | undefined
let container: HTMLDivElement
let media: { matches: boolean; addEventListener: ReturnType<typeof vi.fn>; removeEventListener: ReturnType<typeof vi.fn> }

function mount() {
  container = document.createElement('div')
  app = createApp({
    setup() {
      usePublicSmoothScroll()
      return () => h('main', '用户端页面')
    },
  })
  app.mount(container)
}

beforeEach(() => {
  vi.resetAllMocks()
  createLenis.mockImplementation(function () { return { destroy } })
  media = { matches: false, addEventListener: vi.fn(), removeEventListener: vi.fn() }
  vi.stubGlobal('matchMedia', vi.fn(() => media))
})

afterEach(() => {
  app?.unmount()
  app = undefined
  container?.remove()
  document.body.classList.remove('el-popup-parent--hidden')
  vi.unstubAllGlobals()
})

describe('public smooth scrolling lifecycle', () => {
  it('enables wheel inertia only while the public layout is mounted, and cleans up when leaving it', () => {
    expect(createLenis).not.toHaveBeenCalled()
    expect(document.documentElement.classList.contains('public-scroll')).toBe(false)
    mount()
    expect(createLenis).toHaveBeenCalledOnce()
    expect(createLenis).toHaveBeenCalledWith(expect.objectContaining({ autoRaf: true, smoothWheel: true, syncTouch: false }))
    expect(document.documentElement.classList.contains('public-scroll')).toBe(true)

    app!.unmount()
    app = undefined
    expect(destroy).toHaveBeenCalledOnce()
    expect(document.documentElement.classList.contains('public-scroll')).toBe(false)
    expect(media.removeEventListener).toHaveBeenCalledWith('change', media.addEventListener.mock.calls[0]![1])
  })

  it('respects reduced motion at startup and reacts to preference changes without duplicate instances', () => {
    media.matches = true
    mount()
    expect(createLenis).not.toHaveBeenCalled()
    const onChange = media.addEventListener.mock.calls[0]![1] as () => void
    media.matches = false
    onChange()
    onChange()
    expect(createLenis).toHaveBeenCalledOnce()
    media.matches = true
    onChange()
    expect(destroy).toHaveBeenCalledOnce()
    media.matches = false
    onChange()
    expect(createLenis).toHaveBeenCalledTimes(2)
    app!.unmount()
    app = undefined
    expect(destroy).toHaveBeenCalledTimes(2)
  })

  it('lets text areas, nested controls and modal overlays keep their native scrolling', () => {
    mount()
    const { prevent } = createLenis.mock.calls[0]![0] as { prevent: (element: HTMLElement) => boolean }
    const field = document.createElement('textarea')
    const panel = document.createElement('div')
    panel.className = 'el-scrollbar__wrap'
    const nested = document.createElement('div')
    panel.append(nested)
    const content = document.createElement('article')
    expect(prevent(field)).toBe(true)
    expect(prevent(nested)).toBe(true)
    expect(prevent(content)).toBe(false)
    document.body.classList.add('el-popup-parent--hidden')
    expect(prevent(content)).toBe(true)
  })
})
