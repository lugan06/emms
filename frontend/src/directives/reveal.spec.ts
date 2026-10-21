import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, withDirectives, type App } from 'vue'
import { vReveal } from './reveal'

class ObserverMock {
  static instances: ObserverMock[] = []
  observe = vi.fn()
  unobserve = vi.fn()
  disconnect = vi.fn()

  constructor(private callback: IntersectionObserverCallback) {
    ObserverMock.instances.push(this)
  }

  enter(element: HTMLElement, isIntersecting = true) {
    const bounds = element.getBoundingClientRect()
    this.callback([{
      target: element, isIntersecting, boundingClientRect: bounds, intersectionRect: bounds,
      intersectionRatio: isIntersecting ? 1 : 0, rootBounds: null, time: 0,
    }], this as unknown as IntersectionObserver)
  }
}

let app: App | undefined
let container: HTMLDivElement
let media: { matches: boolean; addEventListener: ReturnType<typeof vi.fn>; removeEventListener: ReturnType<typeof vi.fn> }

function mount(count = 1) {
  container = document.createElement('div')
  document.body.append(container)
  app = createApp({
    render: () => h('section', Array.from({ length: count }, (_, index) =>
      withDirectives(h('article', [h('button', '查看详情')]), [[vReveal, index * 100]]))),
  })
  app.mount(container)
  return [...container.querySelectorAll<HTMLElement>('article')]
}

beforeEach(() => {
  ObserverMock.instances = []
  media = { matches: false, addEventListener: vi.fn(), removeEventListener: vi.fn() }
  vi.stubGlobal('IntersectionObserver', ObserverMock)
  vi.stubGlobal('matchMedia', vi.fn(() => media))
})

afterEach(() => {
  app?.unmount()
  app = undefined
  container?.remove()
  vi.unstubAllGlobals()
})

describe('scroll reveal directive', () => {
  it('shares one observer and reveals each element once, releasing it when the last element is shown', () => {
    const [first, second] = mount(2)
    expect(ObserverMock.instances).toHaveLength(1)
    const observer = ObserverMock.instances[0]!
    expect(observer.observe).toHaveBeenCalledTimes(2)
    expect(first!.classList.contains('scroll-reveal')).toBe(true)
    expect(second!.style.getPropertyValue('--reveal-delay')).toBe('100ms')
    observer.enter(first!, false)
    expect(first!.classList.contains('is-revealed')).toBe(false)
    observer.enter(first!)
    expect(first!.classList.contains('is-revealed')).toBe(true)
    expect(observer.unobserve).toHaveBeenCalledWith(first)
    expect(observer.disconnect).not.toHaveBeenCalled()
    observer.enter(first!, false)
    expect(first!.classList.contains('is-revealed')).toBe(true)
    observer.enter(second!)
    expect(second!.classList.contains('is-revealed')).toBe(true)
    expect(observer.disconnect).toHaveBeenCalledOnce()
    expect(media.removeEventListener).toHaveBeenCalledWith('change', media.addEventListener.mock.calls[0]![1])
  })

  it('cleans up elements that are removed before entering the viewport', () => {
    const [element] = mount()
    const observer = ObserverMock.instances[0]!
    app!.unmount()
    app = undefined
    expect(observer.unobserve).toHaveBeenCalledWith(element)
    expect(observer.disconnect).toHaveBeenCalledOnce()
    expect(element!.classList.contains('scroll-reveal')).toBe(false)
  })

  it.each(['reduced-motion', 'missing-observer'])('keeps content visible without animation for %s', fallback => {
    media.matches = fallback === 'reduced-motion'
    if (fallback === 'missing-observer') vi.stubGlobal('IntersectionObserver', undefined)
    const [element] = mount()
    expect(element!.classList.contains('scroll-reveal')).toBe(false)
    expect(ObserverMock.instances).toHaveLength(0)
  })

  it('reveals pending content immediately when reduced motion is enabled', () => {
    const elements = mount(2)
    media.matches = true
    const onChange = media.addEventListener.mock.calls[0]![1] as () => void
    onChange()
    expect(elements.every(element => element.classList.contains('is-revealed'))).toBe(true)
    expect(ObserverMock.instances[0]!.disconnect).toHaveBeenCalledOnce()
  })

  it('shows a card when keyboard focus reaches its link or button', () => {
    const [element] = mount()
    element!.querySelector('button')!.focus()
    expect(element!.classList.contains('is-revealed')).toBe(true)
    expect(ObserverMock.instances[0]!.unobserve).toHaveBeenCalledWith(element)
  })
})
