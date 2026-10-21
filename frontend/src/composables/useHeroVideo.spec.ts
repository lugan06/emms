import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, nextTick, ref, type App } from 'vue'
import { useHeroVideo } from './useHeroVideo'

function mediaQuery(media: string) {
  const listeners = new Set<() => void>()
  return {
    media,
    matches: false,
    listeners,
    addEventListener: vi.fn((_event: string, listener: () => void) => listeners.add(listener)),
    removeEventListener: vi.fn((_event: string, listener: () => void) => listeners.delete(listener)),
    change(matches: boolean) {
      this.matches = matches
      listeners.forEach(listener => listener())
    },
  }
}

let app: App | undefined
let container: HTMLDivElement
let reducedMotion: ReturnType<typeof mediaQuery>
let mobile: ReturnType<typeof mediaQuery>

function mount() {
  const source = ref('/uploads/hero.mp4')
  let playback!: ReturnType<typeof useHeroVideo>
  container = document.createElement('div')
  app = createApp({
    setup() {
      playback = useHeroVideo(source)
      return () => h('div')
    },
  })
  app.mount(container)
  return { source, playback }
}

beforeEach(() => {
  reducedMotion = mediaQuery('(prefers-reduced-motion: reduce)')
  mobile = mediaQuery('(max-width: 760px)')
  vi.stubGlobal('matchMedia', vi.fn((query: string) => query === reducedMotion.media ? reducedMotion : mobile))
})

afterEach(() => {
  app?.unmount()
  app = undefined
  container?.remove()
  vi.unstubAllGlobals()
})

describe('hero video playback', () => {
  it.each(['reduced-motion', 'mobile', 'save-data', 'slow-network'])('uses the static cover from the first render for %s', preference => {
    reducedMotion.matches = preference === 'reduced-motion'
    mobile.matches = preference === 'mobile'
    vi.stubGlobal('navigator', {
      connection: { saveData: preference === 'save-data', effectiveType: preference === 'slow-network' ? '2g' : '4g' },
    })

    const { playback } = mount()
    expect(playback.videoAllowed.value).toBe(false)
    expect(playback.videoReady.value).toBe(false)
  })

  it('pauses on preference changes and removes media and resize subscriptions on unmount', () => {
    const removeResize = vi.spyOn(window, 'removeEventListener')
    const { playback } = mount()
    const pause = vi.fn()
    playback.heroVideo.value = { pause } as unknown as HTMLVideoElement
    playback.handleVideoCanPlay()
    expect(playback.videoReady.value).toBe(true)

    mobile.change(true)
    expect(playback.videoAllowed.value).toBe(false)
    expect(playback.videoReady.value).toBe(false)
    expect(pause).toHaveBeenCalledOnce()

    mobile.change(false)
    expect(playback.videoAllowed.value).toBe(true)
    expect(playback.videoReady.value).toBe(false)
    app!.unmount()
    app = undefined
    expect(mobile.listeners.size).toBe(0)
    expect(reducedMotion.listeners.size).toBe(0)
    expect(removeResize).toHaveBeenCalledWith('resize', expect.any(Function))
    removeResize.mockRestore()
  })

  it('recovers from a failed video when a new source is configured', async () => {
    const { source, playback } = mount()
    playback.handleVideoError()
    expect(playback.videoAllowed.value).toBe(false)
    source.value = '/uploads/new-hero.mp4'
    await nextTick()
    expect(playback.videoAllowed.value).toBe(true)
    expect(playback.videoReady.value).toBe(false)
    playback.handleVideoCanPlay()
    expect(playback.videoReady.value).toBe(true)
  })

  it('reacts to data-saving changes and removes the connection listener', () => {
    const connection = {
      saveData: false,
      addEventListener: vi.fn<(event: string, listener: () => void) => void>(),
      removeEventListener: vi.fn<(event: string, listener: () => void) => void>(),
    }
    vi.stubGlobal('navigator', { connection })
    const { playback } = mount()
    const onChange = connection.addEventListener.mock.calls[0]![1] as () => void
    connection.saveData = true
    onChange()
    expect(playback.videoAllowed.value).toBe(false)
    connection.saveData = false
    onChange()
    expect(playback.videoAllowed.value).toBe(true)
    app!.unmount()
    app = undefined
    expect(connection.removeEventListener).toHaveBeenCalledWith('change', onChange)
  })
})
